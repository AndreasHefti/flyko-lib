package com.inari.firefly.physics.contact

import com.inari.firefly.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.MATERIAL_ASPECT_GROUP
import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.entity.Entity
import com.inari.firefly.physics.contact.Contact.Companion.NO_CONTACT
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.*
import kotlin.jvm.JvmField
import kotlin.math.floor


class FullContactScan internal constructor(
    @JvmField internal var constraintRef: Int
) : ContactScan {

    override val constraint = ContactSystem.constraints[constraintRef]
    @JvmField internal val contactTypes = CONTACT_TYPE_ASPECT_GROUP.createAspects()
    @JvmField internal val materialTypes = MATERIAL_ASPECT_GROUP.createAspects()
    @JvmField internal val intersectionMask = BitMask(width = constraint.bounds.width, height = constraint.bounds.height)
    private val contacts: DynArray<Contact> = DynArray.of(1, 1)

    init {
        if (constraint.isCircle)
            intersectionMask.reset(0, 0, constraint.bounds.radius * 2, constraint.bounds.radius * 2)
    }

    val width: Int
        get() = constraint.bounds.width
    val height: Int
        get() = constraint.bounds.height
    val contactMask: BitMask
        get() = intersectionMask

    fun allContacts(): DynArrayRO<Contact> = contacts
    override fun hasAnyContact(): Boolean = !contacts.isEmpty
    fun hasContactOfType(types: Aspects): Boolean = contactTypes.intersects(types)
    override fun hasContactOfType(type: Aspect): Boolean = contactTypes.contains(type)
    fun hasMaterialContact(materials: Aspects): Boolean = materialTypes.intersects(materials)
    fun hasMaterialContact(material: Aspect): Boolean  = materialTypes.contains(material)
    fun hasContactAt(p: Vector2i): Boolean = intersectionMask.getBit(p.x, p.y)
    fun hasContact(p1: Vector2i, p2: Vector2i): Boolean = intersectionMask.getBit(p1.x, p1.y) || intersectionMask.getBit(p2.x, p2.y)
    fun hasContact(x: Int, y: Int): Boolean = intersectionMask.getBit(x, y)
    fun hasContactOfType(contactType: Aspect, p: Vector2i): Boolean = hasContactOfType(contactType, p.x, p.y)

    fun hasContactOfType(contactType: Aspect, x: Int, y: Int): Boolean {
        if (contactType !in contactTypes)
            return false

        var i = 0
        while (i < contacts.capacity) {
            val contact = contacts[i++] ?: continue
            if (contact.contactType.aspectIndex != contactType.aspectIndex)
                continue
            if (contact.hasContact(x, y))
                return true
        }

        return false
    }

    fun hasContactTypeExclusive(contactType: Aspect, p: Vector2i): Boolean = hasContactTypeExclusive(contactType, p.x, p.y)
    fun hasContactTypeExclusive(contactType: Aspect, x: Int, y: Int): Boolean {
        if (contactTypes.contains(contactType))
            return false

        var i = 0
        while (i < contacts.capacity) {
            val contact = contacts[i++] ?: continue
            if (contact.contactType.aspectIndex == contactType.aspectIndex)
                continue
            if (contact.hasContact(x, y))
                return true
        }

        return false
    }

    fun hasContact(material: Aspect, p: Vector2i): Boolean = hasContact(material, p.x, p.y)
    fun hasContact(material: Aspect, x: Int, y: Int): Boolean {
        if (!materialTypes.contains(material))
            return false

        var i = 0
        while (i < contacts.capacity) {
            val contact = contacts[i++] ?: continue
            if (contact.materialType.aspectIndex != material.aspectIndex)
                continue
            if (contact.hasContact(x, y))
                return true
        }

        return false
    }

    fun hasContactExclusive(material: Aspect, p: Vector2i): Boolean = hasContactExclusive(material, p.x, p.y)
    fun hasContactExclusive(material: Aspect, x: Int, y: Int): Boolean {
        if (materialTypes.contains(material))
            return false

        var i = 0
        while (i < contacts.capacity) {
            val contact = contacts[i++] ?: continue
            if (contact.materialType.aspectIndex == material.aspectIndex)
                continue
            if (contact.hasContact(x, y))
                return true
        }

        return false
    }

    fun get(pos: Vector2i): Contact = this[pos.x, pos.y]
    operator fun get(x: Int, y: Int): Contact {
        var i = 0
        while (i < contacts.capacity) {
            val contact = contacts[i++] ?: continue
            if (contact.intersects(x, y))
                return contact
        }

        return NO_CONTACT
    }

    fun getFirstContactOfType(contactType: Aspect): Contact {
        var i = 0
        while (i < contacts.capacity) {
            val contact = contacts[i++] ?: continue
            if (contact.contactType === contactType)
                return contact
        }

        return NO_CONTACT
    }

    fun getFirstContactOfMaterial(materialType: Aspect): Contact {
        var i = 0
        while (i < contacts.capacity) {
            val contact = contacts[i++] ?: continue
            if (contact.materialType === materialType)
                return contact
        }

        return NO_CONTACT
    }

    override fun clear() {
        var i = 0
        while (i < contacts.capacity) {
            val contact = contacts[i++] ?: continue
            ContactSystem.ContactsPool.disposeContact(contact)
        }
        contacts.clear()
        contactTypes.clear()
        materialTypes.clear()
        intersectionMask.clearMask()
    }

    override fun scanFullContact(worldBounds: Vector4i, otherEntity: Entity, otherWorldPos: Vector2f) {
        val otherContact = otherEntity[EContact]

        if (!constraint.match(otherContact))
            return

        val contact = ContactSystem.ContactsPool.getContactFromPool()
        contact.entityId = otherEntity.index
        contact.contactType = otherContact.contactType
        contact.materialType = otherContact.material
        contact.worldBounds.x = (floor(otherWorldPos.x.toDouble()) + otherContact.bounds.x).toInt()
        contact.worldBounds.y = (floor(otherWorldPos.y.toDouble()) + otherContact.bounds.y).toInt()
        contact.worldBounds.width = otherContact.bounds.width
        contact.worldBounds.height = otherContact.bounds.height

        if (otherContact.isCircle)
            scanOtherCircle(worldBounds, contact, otherContact)
        else
            scanOtherRectangle(worldBounds, contact, otherContact)
    }

    private fun scanOtherCircle(
        worldBounds: Vector4i,
        contact: Contact,
        otherContact: EContact
    ) {
        if (constraint.isCircle && !GeomUtils.intersectCircle(contact.worldBounds as Vector3i, worldBounds as Vector3i)) {
            ContactSystem.ContactsPool.disposeContact(contact)
            return
        } else if (!GeomUtils.intersectCircle(contact.worldBounds as Vector3i, worldBounds)) {
            ContactSystem.ContactsPool.disposeContact(contact)
            return
        }

        // aproximated rectangle for circle
        circleToAproximatedRect(contact.worldBounds)
        // if this constraint is also a cirlce, do the same
        if (constraint.isCircle)
            circleToAproximatedRect(worldBounds)

        GeomUtils.intersection(
            worldBounds,
            contact.worldBounds,
            contact.intersectionBounds
        )

        if (!scan(contact, otherContact.mask, worldBounds))
            ContactSystem.ContactsPool.disposeContact(contact)
    }

    private fun scanOtherRectangle(
        worldBounds: Vector4i,
        contact: Contact,
        otherContact: EContact
    ) {
        if (constraint.isCircle &&!GeomUtils.intersectCircle(contact.worldBounds as Vector3i, worldBounds as Vector3i)) {
            ContactSystem.ContactsPool.disposeContact(contact)
            return
        }

        if (constraint.isCircle)
            circleToAproximatedRect(worldBounds)

        // take the rectangular intersection area and store it in contact.intersectionBounds
        GeomUtils.intersection(
            worldBounds,
            contact.worldBounds,
            contact.intersectionBounds
        )

        // if we don't have any rectangular intersection skip
        if (GeomUtils.area(contact.intersectionBounds) <= 0) {
            ContactSystem.ContactsPool.disposeContact(contact)
            return
        }

        if (!scan(contact, otherContact.mask, worldBounds))
            ContactSystem.ContactsPool.disposeContact(contact)
    }

    private fun circleToAproximatedRect(bounds: Vector4i) {
        val cwh = (bounds.radius * 1.9f).toInt()
        bounds.x = bounds.x - bounds.radius
        bounds.y = bounds.y - bounds.radius
        bounds.width = cwh
        bounds.height = cwh
    }


    private val checkPivot = Vector4i()
    internal fun scan(contact: Contact, otherBitMask: BitMask, worldBounds: Vector4i): Boolean {
        // normalize the intersection to origin of coordinate system
        contact.intersectionBounds.x -= worldBounds.x
        contact.intersectionBounds.y -= worldBounds.y

        if (otherBitMask.isEmpty) {
            addFullContact(contact)
            return true
        }

        checkPivot.x = worldBounds.x - contact.worldBounds.x
        checkPivot.y = worldBounds.y - contact.worldBounds.y
        checkPivot.width = worldBounds.width
        checkPivot.height = worldBounds.height

        if (BitMask.createIntersectionMask(checkPivot, otherBitMask, contact.intersectionMask, true)) {
            addFullContact(contact)
            return true
        }

        return false
    }

    private fun addFullContact(contact: Contact) {
        if (!contact.intersectionMask.isEmpty)
            intersectionMask.or(contact.intersectionMask)
        else
            intersectionMask.setRegion(contact.intersectionBounds, true)

        if (contact.contactType !== UNDEFINED_CONTACT_TYPE)
            contactTypes + contact.contactType

        if (contact.materialType != UNDEFINED_MATERIAL)
            materialTypes + contact.materialType

        contacts.add(contact)
    }

}