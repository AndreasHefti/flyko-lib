package com.inari.firefly.physics.contact

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.FFContext
import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.*
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGridSystem
import com.inari.firefly.graphics.view.ViewEvent
import com.inari.firefly.graphics.view.ViewLayerMapping
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.MoveEvent
import com.inari.util.Consumer
import com.inari.util.Named
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet
import com.inari.util.geom.*
import com.inari.util.indexed.Indexed
import kotlin.jvm.JvmField
import kotlin.math.ceil
import kotlin.math.floor


object ContactSystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
        ContactMap,
        ContactConstraint,
        CollisionResolver)

    @JvmField val contactMapViewLayer: ViewLayerMapping<ContactMap> = ViewLayerMapping.of()
    @JvmField val contactMaps = ComponentSystem.createComponentMapping(
            ContactMap,
            listener = { contactMap, action -> when (action) {
                CREATED -> contactMapViewLayer.add(contactMap)
                DELETED -> contactMapViewLayer.delete(contactMap)
                else -> DO_NOTHING
            } }
        )
    @JvmField val constraints = ComponentSystem.createComponentMapping(ContactConstraint)

    @JvmField val collisionResolver = ComponentSystem.createComponentMapping(CollisionResolver)

    private val viewListener: Consumer<ViewEvent> = { event ->
        when(event.type) {
            ViewEvent.Type.VIEW_DELETED -> contactMaps.deleteAll { c -> event.id.instanceId == c?.viewRef }
            else -> DO_NOTHING
        }
    }

    private val moveListener: Consumer<MoveEvent> = { moveEvent ->
        updateContactMaps(moveEvent.entities)
    }

    // Contains all entity ids that has contact scans defined and are active
    // They are all processed during one contact scan cycle. A contact scan is triggered by a move event
    private val entitiesWithScan = BitSet()
    private val entityActivationListener: EntityEventListener = object: EntityEventListener {
        override fun entityActivated(entity: Entity) {
            contactMapViewLayer[entity[ETransform]]?.add(entity)
            if (entity[EContact].contactScans.hasAnyScan)
                entitiesWithScan[entity.index] = true
        }
        override fun entityDeactivated(entity: Entity) {
            contactMapViewLayer[entity[ETransform]]?.remove(entity)
            if (entity[EContact].contactScans.hasAnyScan)
                entitiesWithScan[entity.index] = false
        }
        override fun match(aspects: Aspects): Boolean =
            EContact in aspects &&
            ETransform in aspects &&
            ETile !in aspects
    }

    init {
        FFContext.registerListener(ViewEvent, viewListener)
        FFContext.registerListener(EntityEvent, entityActivationListener)
        FFContext.registerListener(MoveEvent, moveListener)
    }

    fun createContacts(constraint: ContactConstraint): FullContactScan =
        createContacts(constraint.index)

    fun createContacts(constraint: CompId): FullContactScan =
        createContacts(constraint.instanceId)

    fun createContacts(constraint: Int): FullContactScan =
        if (constraint in constraints)
            FullContactScan(constraint)
        else
            throw IllegalArgumentException("No ContactConstraint found for id: $constraint")


    fun updateContacts(entityId: Int) {
        val entity = EntitySystem[entityId]
        val contacts = entity[EContact]
        if (!contacts.contactScans.hasAnyScan)
            return

        scanContacts(entity, contacts)
    }

    fun updateContacts(indexed: Indexed) {
        updateContacts(indexed.index)
    }

    fun updateContacts(entityName: String) {
        val entity = EntitySystem[entityName]
        val contacts = entity[EContact]
        if (!contacts.contactScans.hasAnyScan)
            return

        scanContacts(entity, contacts)
    }

    fun updateContacts(entityName: Named) {
        updateContacts(entityName.name)
    }

    fun updateContacts(entityId: Int, constraintName: String) {
        val entity = EntitySystem[entityId]
        val contacts = entity[EContact]
        val contactConstraint = constraints[constraintName]
        val scan = contacts.contactScans.scans[contactConstraint.index] ?: return

        updateContacts(entity, scan)
    }

    private fun updateContactMaps(entities: BitSet) {
        // first we have to update all moved entities within the registered ContactMap's
        var i = entities.nextSetBit(0)
        while (i >= 0) {
            val entity = EntitySystem[i]
            i = entities.nextSetBit(i + 1)
            if (EContact !in entity.aspects)
                continue

            contactMapViewLayer[entity[ETransform]]?.update(entity)
        }

        // then we can update the contacts on the new positions
        updateContacts()
    }

    private fun updateContacts() {
        var i = entitiesWithScan.nextSetBit(0)
        while (i >= 0) {
            val entity = EntitySystem[i]
            i = entitiesWithScan.nextSetBit(i + 1)
            val contacts = entity[EContact]
            if (!contacts.contactScans.hasAnyScan)
                continue

            scanContacts(entity, contacts)

            if (contacts.collisionResolverRef >= 0)
                collisionResolver[contacts.collisionResolverRef].resolve(entity, contacts, contacts.contactScans)

            if (contacts.notifyContacts && contacts.contactScans.hasAnyContact()) {
                ContactEvent.contactEvent.entityId = entity.index
                FFContext.notify(ContactEvent.contactEvent)
            }

            contactMapViewLayer[entity[ETransform]]?.update(entity)
        }
    }

    private fun scanContacts(entity: Entity, contactsComp: EContact) {
        var i = 0
        while (i < contactsComp.contactScans.scans.capacity) {
            val c = contactsComp.contactScans.scans[i++] ?: continue
            updateContacts(entity, c)
        }
    }

    private val originWorldBounds = ContactBounds(circle = Vector3i())
    private val otherWorldBounds = ContactBounds(circle = Vector3i())
    private fun updateContacts(entity: Entity, contactScan: ContactScan) {
        val constraint = contactScan.constraint
        val transform = entity[ETransform]
        val movement = entity[EMovement]

        var layerRef = constraint.layerRef
        if (layerRef < 0)
            layerRef = transform.layerRef

        contactScan.clear()

        // apply bounds of the contact shape within world coordinate system
        val position = getWorldPos(entity, transform)
        originWorldBounds.bounds(
            (if (movement.velocity.v0 > 0) ceil(position.x.toDouble()).toInt() else floor(position.x.toDouble()).toInt()) + constraint.bounds.x,
            (if (movement.velocity.v1 > 0) ceil(position.y.toDouble()).toInt() else floor(position.y.toDouble()).toInt()) + constraint.bounds.y,
        )
        if (constraint.isCircle) {
            originWorldBounds.circle!!.x = originWorldBounds.bounds.x
            originWorldBounds.circle.y = originWorldBounds.bounds.y
            originWorldBounds.circle.radius = constraint.bounds.radius
            originWorldBounds.bounds.x = originWorldBounds.bounds.x - constraint.bounds.radius
            originWorldBounds.bounds.y = originWorldBounds.bounds.y - constraint.bounds.radius
        } else {
            originWorldBounds.bounds.width = constraint.bounds.width
            originWorldBounds.bounds.height = constraint.bounds.height
        }

        scanTileContacts(entity, transform.viewRef, layerRef, contactScan)
        scanSpriteContacts(entity, transform.viewRef, layerRef, contactScan)
    }

    private val tempPos = Vector2f()
    private fun scanTileContacts(entity: Entity, viewRef: Int, layerRef: Int, contactScan: ContactScan) {
        if (!TileGridSystem.existsAny(viewRef, layerRef))
            return

        val tileGrids = TileGridSystem[viewRef, layerRef] ?: return

        tileGrids.forEach {
            val iterator = it.tileGridIterator(originWorldBounds.bounds)
            while (iterator.hasNext()) {
                val otherEntityRef = iterator.next()
                if (entity.index == otherEntityRef)
                    continue

                val otherEntity = EntitySystem[otherEntityRef]
                if (EContact !in otherEntity.aspects)
                    continue

                val otherTransform = otherEntity[ETransform]
                val otherContact = otherEntity[EContact]

                //apply bounds of the other contact shape within world coordinate system
                tempPos(
                    iterator.worldPosition.x + otherTransform.position.x,
                    iterator.worldPosition.y + otherTransform.position.y
                )
                applyContactBounds(otherWorldBounds, otherContact, tempPos.x, tempPos.y)
                contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, otherEntity.index)
            }
        }
    }

    private fun scanSpriteContacts(entity: Entity, viewRef: Int, layerRef: Int, contactScan: ContactScan) {
        if (!contactMapViewLayer.contains(viewRef, layerRef))
            return

        val iterator = contactMapViewLayer[viewRef, layerRef]!![originWorldBounds.bounds, entity]
        while (iterator.hasNext()) {
            val otherEntity = EntitySystem[iterator.next()]
            val otherContact = otherEntity[EContact]
            val otherWorldPos = getWorldPos(otherEntity, otherEntity[ETransform])

            if (EMultiplier in otherEntity.aspects) {
                val multiplier = otherEntity[EMultiplier]
                val iterator = multiplier.positions.iterator()
                while (iterator.hasNext()) {
                    applyContactBounds(
                        otherWorldBounds,
                        otherContact,
                        otherWorldPos.x + iterator.next(),
                        otherWorldPos.y + iterator.next())
                    contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, otherEntity.index)
                }
            } else {
                applyContactBounds(otherWorldBounds, otherContact, otherWorldPos.x, otherWorldPos.y)
                contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, otherEntity.index)
            }
        }
    }

    private fun applyContactBounds(
        contactBounds: ContactBounds,
        contactDef: EContact,
        worldPosX: Float,
        worldPosY: Float
    ) {
        if (contactDef.isCircle)
            contactBounds.applyCircle(
                floor(worldPosX).toInt() + contactDef.bounds.x,
                floor(worldPosY).toInt() + contactDef.bounds.y,
                contactDef.bounds.radius
            )
        else
            contactBounds.applyRectangle(
                floor(worldPosX).toInt() + contactDef.bounds.x,
                floor(worldPosY).toInt() + contactDef.bounds.y,
                contactDef.bounds.width,
                contactDef.bounds.height
            )
        if (!contactDef.mask.isEmpty)
            contactBounds.applyBitMask(contactDef.mask)
        else
            contactBounds.resetBitmask()
    }

    override fun clearSystem() {
        contactMaps.clear()
        constraints.clear()
        collisionResolver.clear()
    }

    private fun getWorldPos(entity: Entity, transform: ETransform): Vector2f {
        return if (EChild in entity.aspects) {
            addTransformPos(entity[EChild].int_parent)
            tempPos
        } else
            transform.position
    }

    private fun addTransformPos(parent: Int) {
        val parentEntity = Entity[parent]
        tempPos + parentEntity[ETransform].position
        if (EChild in parentEntity.aspects)
            addTransformPos(parentEntity[EChild].int_parent)
    }
}

internal class ContactBounds (
    @JvmField val bounds: Vector4i = Vector4i(),
    @JvmField val circle: Vector3i? = null,
) {
    var bitmask: BitMask? = null
        internal set
    val isCircle get() = circle != null && circle.radius > 0
    val hasBitmask get() = bitmask != null && !bitmask!!.isEmpty

    fun applyRectangle(x: Int, y: Int, w: Int, h: Int) = bounds(x, y, w, h)
    fun applyRectangle(rect: Vector4i) = bounds(rect)

    fun applyCircle(x: Int, y: Int, r: Int) {
        // create rectangle bounds for circle too
        val length = r * 2
        bounds(x - r, y - r, length, length)
        circle!!(x, y, r)
    }
    fun applyCircle(rect: Vector3i) {
        // create rectangle bounds for circle too
        val length = rect.radius * 2
        bounds(rect.x - rect.radius, rect.y - rect.radius, length, length)
        circle!!(rect)
    }
    fun applyBitMask(bitmask: BitMask) {
        this.bitmask = bitmask
    }
    fun resetBitmask() {
        this.bitmask = null
    }
}

internal object ContactsPool {
    private val CONTACTS_POOL = ArrayDeque<Contact>()

    internal fun disposeContact(contact: Contact) {
        contact.entityId = -1
        contact.intersectionMask.clearMask()
        contact.worldBounds(0, 0, 0, 0)
        contact.contactType = UNDEFINED_CONTACT_TYPE
        contact.materialType = UNDEFINED_MATERIAL
        contact.intersectionBounds(0, 0, 0, 0)

        CONTACTS_POOL.add(contact)
    }

    internal fun getContactFromPool(): Contact =
        if (CONTACTS_POOL.isEmpty())
            Contact()
        else
            CONTACTS_POOL.removeFirst()
}