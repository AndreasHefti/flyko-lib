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
import com.inari.util.geom.GeomUtils.area
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

    private val worldBounds = Vector4i()
    private val worldTempPos = Vector2f()
    private fun updateContacts(entity: Entity, contactScan: ContactScan) {
        val constraint = contactScan.constraint
        val transform = entity[ETransform]
        val movement = entity[EMovement]

        var layerRef = constraint.layerRef
        if (layerRef < 0)
            layerRef = transform.layerRef

        contactScan.clear()
        val position = getWorldPos(entity, transform);
        worldBounds(
            (if (movement.velocity.v0 > 0) ceil(position.x.toDouble()).toInt() else floor(position.x.toDouble()).toInt()) + constraint.bounds.x,
            (if (movement.velocity.v1 > 0) ceil(position.y.toDouble()).toInt() else floor(position.y.toDouble()).toInt()) + constraint.bounds.y,
        )
        if (constraint.isCircle) {
            worldBounds.radius = constraint.bounds.radius
            worldBounds.v3 = 0
        } else {
            worldBounds.width = constraint.bounds.width
            worldBounds.height = constraint.bounds.height
        }

        scanTileContacts(entity, transform, layerRef, contactScan)
        scanSpriteContacts(entity, transform, layerRef, contactScan)
    }

    private fun scanTileContacts(entity: Entity, transform: ETransform, layerRef: Int, contactScan: ContactScan) {
        if (!TileGridSystem.existsAny(transform.viewRef, layerRef))
            return

        val tileGrids = TileGridSystem[transform.viewRef, layerRef] ?: return

        tileGrids.forEach {
            val iterator = it.tileGridIterator(worldBounds)
            while (iterator.hasNext()) {
                val otherEntityRef = iterator.next()
                if (entity.index == otherEntityRef)
                    continue

                val otherEntity = EntitySystem[otherEntityRef]
                if (EContact !in otherEntity.aspects)
                    continue

                val otherTransform = otherEntity[ETransform]
                worldTempPos(
                    iterator.worldPosition.x + otherTransform.position.x,
                    iterator.worldPosition.y + otherTransform.position.y)
                contactScan.scanFullContact(worldBounds, otherEntity, worldTempPos)
            }
        }
    }

    private fun scanSpriteContacts(entity: Entity, transform: ETransform, layerRef: Int, contactScan: ContactScan) {
        if (!contactMapViewLayer.contains(transform.viewRef, layerRef))
            return

        val iterator = contactMapViewLayer[transform.viewRef, layerRef]!![worldBounds, entity]
        while (iterator.hasNext()) {
            val otherEntity = EntitySystem[iterator.next()]
            val otherWorldPos = getWorldPos(otherEntity, otherEntity[ETransform])
            contactScan.scanFullContact(worldBounds, otherEntity, otherWorldPos)
        }
    }

    override fun clearSystem() {
        contactMaps.clear()
        constraints.clear()
        collisionResolver.clear()
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

    private fun getWorldPos(entity: Entity, transform: ETransform): Vector2f {
        return if (EChild in entity.aspects) {
            addTransformPos(entity[EChild].int_parent)
            worldTempPos
        } else
            transform.position
    }

    private fun addTransformPos(parent: Int) {
        val parentEntity = Entity[parent]
        worldTempPos + parentEntity[ETransform].position
        if (EChild in parentEntity.aspects)
            addTransformPos(parentEntity[EChild].int_parent)
    }
}