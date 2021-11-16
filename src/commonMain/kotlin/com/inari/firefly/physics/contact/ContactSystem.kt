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
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet
import com.inari.util.geom.*
import com.inari.util.geom.GeomUtils.area
import com.inari.util.indexed.Indexed
import kotlin.jvm.JvmField
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

    private val entityActivationListener: EntityEventListener = object: EntityEventListener {
        override fun entityActivated(entity: Entity) {
            contactMapViewLayer[entity[ETransform]]?.add(entity)
        }
        override fun entityDeactivated(entity: Entity) {
            contactMapViewLayer[entity[ETransform]]?.remove(entity)
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

    fun createContacts(constraint: ContactConstraint): Contacts =
        createContacts(constraint.index)

    fun createContacts(constraint: CompId): Contacts =
        createContacts(constraint.instanceId)

    fun createContacts(constraint: Int): Contacts =
        if (constraint in constraints)
            Contacts(constraint)
        else
            throw IllegalArgumentException("No ContactConstraint found for id: $constraint")


    fun updateContacts(entityId: Int) {
        val entity = EntitySystem[entityId]
        val contacts = entity[EContact]
        if (contacts.contactScan.contacts.isEmpty)
            return

        scanContacts(entity, contacts)
    }

    fun updateContacts(indexed: Indexed) {
        updateContacts(indexed.index)
    }

    fun updateContacts(entityName: String) {
        val entity = EntitySystem[entityName]
        val contacts = entity[EContact]
        if (contacts.contactScan.contacts.isEmpty)
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
        val constraint = contacts.contactScan.contacts[contactConstraint.index] ?: return

        updateContacts(entity, constraint)
    }

    fun updateContacts(indexed: Indexed, constraintName: String) {
        updateContacts(indexed.index, constraintName)
    }

    fun updateContacts(entityName: String, constraintName: String) {
        val entity = EntitySystem[entityName]
        val contacts = entity[EContact]
        val contactConstraint = constraints[constraintName]
        val constraint = contacts.contactScan.contacts[contactConstraint.index] ?: return

        updateContacts(entity, constraint)
    }

    fun updateContacts(entityName: Named, constraintName: String) {
        updateContacts(entityName.name, constraintName)
    }

    fun updateContacts(entityId: Int, contacts: Contacts) {
        updateContacts(EntitySystem[entityId], contacts)
    }

    fun updateContacts(indexed: Indexed, contacts: Contacts) {
        updateContacts(indexed.index, contacts)
    }

    fun updateContacts(entityName: String, contacts: Contacts) {
        updateContacts(EntitySystem[entityName], contacts)
    }

    fun updateContacts(entityName: Named, contacts: Contacts) {
        updateContacts(entityName.name, contacts)
    }

    private val tmpEntityKeyMap = BitSet()
    private fun updateContactMaps(entities: BitSet) {
        // first we have to update all moved entities within the registered ContactMap's
        tmpEntityKeyMap.clear()
        var i = entities.nextSetBit(0)
        while (i >= 0) {
            val entity = EntitySystem[i]
            i = entities.nextSetBit(i + 1)
            if (EContact !in entity.aspects)
                continue

            tmpEntityKeyMap.set(entity.index)
            contactMapViewLayer[entity[ETransform]]?.update(entity)
        }

        // then we can update the contacts on the new positions
        updateContacts()
    }

    private fun updateContacts() {
        var i = tmpEntityKeyMap.nextSetBit(0)
        while (i >= 0) {
            val entity = EntitySystem[i]
            i = tmpEntityKeyMap.nextSetBit(i + 1)
            val contacts = entity[EContact]
            if (contacts.contactScan.contacts.isEmpty)
                continue

            scanContacts(entity, contacts)

            if (contacts.collisionResolverRef >= 0)
                collisionResolver[contacts.collisionResolverRef].resolve(entity, contacts, contacts.contactScan)

            if (contacts.notifyContacts && contacts.contactScan.hasAnyContact()) {
                ContactEvent.contactEvent.entityId = entity.index
                FFContext.notify(ContactEvent.contactEvent)
            }

            contactMapViewLayer[entity[ETransform]]?.update(entity)
        }
    }

    private fun scanContacts(entity: Entity, contacts: EContact) {
        var i = 0
        while (i < contacts.contactScan.contacts.capacity) {
            val c = contacts.contactScan.contacts[i++] ?: continue
            updateContacts(entity, c)
        }
    }

    private fun updateContacts(entity: Entity, c: Contacts) {
        val constraint = constraints[c.constraintRef]
        val transform = entity[ETransform]
        val movement = entity[EMovement]

        var layerRef = constraint.layerRef
        if (layerRef < 0)
            layerRef = transform.layerRef

        c.update(
            constraint.bounds,
            getWorldPos(entity, transform),
            movement.velocity
        )

        scanTileContacts(entity, transform, layerRef, c)
        scanSpriteContacts(entity, transform, layerRef, c)
    }

    private fun scanTileContacts(entity: Entity, transform: ETransform, layerRef: Int, c: Contacts) {
        if (!TileGridSystem.exists(transform.viewRef, layerRef))
            return

        val iterator = TileGridSystem[transform.viewRef, layerRef]
            ?.tileGridIterator(c.worldBounds) ?: return

        while (iterator.hasNext()) {
            val otherEntityRef = iterator.next()
            if (entity.index == otherEntityRef)
                continue

            val otherEntity = EntitySystem[otherEntityRef]
            if (EContact !in otherEntity.aspects)
                continue

            val otherTransform = otherEntity[ETransform]
            scanContact(c, otherEntity,
                iterator.worldPosition.x + otherTransform.position.x,
                iterator.worldPosition.y + otherTransform.position.y)
        }
    }

    private fun scanSpriteContacts(entity: Entity, transform: ETransform, layerRef: Int, c: Contacts) {
        if (!contactMapViewLayer.contains(transform.viewRef, layerRef))
            return

        val iterator = contactMapViewLayer[transform.viewRef, layerRef]!![c.worldBounds, entity]
        while (iterator.hasNext()) {
            val otherEntity = EntitySystem[iterator.next()]
            val otherWorldPos = getWorldPos(otherEntity, otherEntity[ETransform])
            scanContact(c, otherEntity, otherWorldPos.x, otherWorldPos.y)
        }

    }

    private val checkPivot = Vector4i()
    private fun scanContact(c: Contacts, otherEntity: Entity, x: Float, y: Float) {
        val otherContact = otherEntity[EContact]
        val constraint = constraints[c.constraintRef]

        if (!constraint.match(otherContact))
            return

        val contact = ContactsPool.createContact(
            otherEntity.index,
            otherContact.material,
            otherContact.contactType,
            (floor(x.toDouble()) + otherContact.bounds.x).toInt(),
            (floor(y.toDouble()) + otherContact.bounds.y).toInt(),
            otherContact.bounds.width,
            otherContact.bounds.height
        )

        GeomUtils.intersection(
            c.worldBounds,
            contact.bounds,
            contact.intersection
        )

        if (area(contact.intersection) <= 0) {
            ContactsPool.disposeContact(contact)
            return
        }

        // normalize the intersection to origin of coordinate system
        contact.intersection.x -= c.worldBounds.x
        contact.intersection.y -= c.worldBounds.y

        if (otherContact.mask.isEmpty) {
            addContact(c, contact)
            return
        }

        checkPivot.x = c.worldBounds.x - contact.bounds.x
        checkPivot.y = c.worldBounds.y - contact.bounds.y
        checkPivot.width = c.worldBounds.width
        checkPivot.height = c.worldBounds.height

        if (BitMask.createIntersectionMask(checkPivot, otherContact.mask, contact.mask, true)) {
            addContact(c, contact)
            return
        }

        ContactsPool.disposeContact(contact)
    }

    private fun addContact(c: Contacts, contact: Contact) {
        if (!GeomUtils.intersect( contact.intersection, c.normalizedContactBounds))
            return

        if (!contact.mask.isEmpty)
            c.intersectionMask.or(contact.mask)
        else
            c.intersectionMask.setRegion(contact.intersection, true)

        if (contact.contact !== UNDEFINED_CONTACT_TYPE)
            c.contactTypes + contact.contact

        if (contact.material != UNDEFINED_MATERIAL)
            c.materialTypes + contact.material

        c.contacts.add(contact)
    }


    override fun clearSystem() {
        contactMaps.clear()
        constraints.clear()
        collisionResolver.clear()
    }

    internal object ContactsPool {
        private val CONTACTS_POOL = ArrayDeque<Contact>()

        internal fun disposeContact(contact: Contact) {
            contact.entity = -1
            contact.intersectionMask.clearMask()
            contact.worldBounds(0, 0, 0, 0)
            contact.contact = UNDEFINED_CONTACT_TYPE
            contact.material = UNDEFINED_MATERIAL
            contact.intersectionBounds(0, 0, 0, 0)

            CONTACTS_POOL.add(contact)
        }

        internal fun createContact(entityId: Int, materialType: Aspect, contactType: Aspect, x: Int, y: Int, width: Int, height: Int): Contact {
            val contact = getContactFromPool()

            contact.entity = entityId
            contact.contact = contactType
            contact.material = materialType
            contact.worldBounds.x = x
            contact.worldBounds.y = y
            contact.worldBounds.width = width
            contact.worldBounds.height = height

            return contact
        }

        private fun getContactFromPool(): Contact =
            if (CONTACTS_POOL.isEmpty())
                Contact()
            else
                CONTACTS_POOL.removeFirst()
    }

    private val worldTempPos = Vector2f()
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