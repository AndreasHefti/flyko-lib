package com.inari.firefly.physics.contact

import com.inari.firefly.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.MATERIAL_ASPECT_GROUP
import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.physics.contact.Contact.Companion.NO_CONTACT
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.*
import kotlin.jvm.JvmField


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

    override fun scanFullContact(
        originWorldContact: ContactBounds,
        otherWorldContact: ContactBounds,
        otherContactDef: EContact,
        otherEntityId: Int
    ) {
        if (!constraint.match(otherContactDef))
            return

        // first check if there is any contact with ordinary shapes
        if (!SimpleContactScan.scanContact(originWorldContact, otherWorldContact))
            return

        // we have contact, create full contact scan
        val contact = ContactSystem.ContactsPool.getContactFromPool()
        contact.entityId = otherEntityId
        contact.contactType = otherContactDef.contactType
        contact.materialType = otherContactDef.material
        contact.worldBounds(otherWorldContact.bounds)
        contact.isCircle = otherContactDef.isCircle
        if (otherContactDef.isCircle)
            contact.worldCircle(otherWorldContact.circle!!)
        else
            contact.worldCircle(0, 0, 0)

        // create intersection clip of origin and other on world ref
        GeomUtils.intersection(
            originWorldContact.bounds,
            contact.worldBounds,
            contact.intersectionBounds
        )

        // and now normalize the intersection clip to origin of coordinate system
        contact.intersectionBounds.x -= originWorldContact.bounds.x
        contact.intersectionBounds.y -= originWorldContact.bounds.y

        // if there is no bitmask defined for the other entity contact bounds
        // just add the contact and contact shape of the other entity to the scan
        if (!otherWorldContact.hasBitmask) {
            addFullContact(contact)
            return
        }

        // scan with with bitmask
        if (!scanBitmask(contact, otherWorldContact.bitmask!!, originWorldContact.bounds))
            ContactSystem.ContactsPool.disposeContact(contact)

    }
    private val clipBounds = Vector4i()
    internal fun scanBitmask(contact: Contact, otherBitMask: BitMask, worldBounds: Vector4i): Boolean {

        // if there is a bitmask defined by the other entity contact bounds
        // we have to scan the origin bounds defined by the constraint with
        // the given bitmask region.

        // TODO what if this is a circle?
        // normalized clip bounds
        clipBounds.x = worldBounds.x - contact.worldBounds.x
        clipBounds.y = worldBounds.y - contact.worldBounds.y
        clipBounds.width = worldBounds.width
        clipBounds.height = worldBounds.height

        // create clipped intersection bitmask stored in contact.intersectionMask
        // and if there is any contact point add the contact
        if (BitMask.createIntersectionMask(clipBounds, otherBitMask, contact.intersectionMask, true)) {
            addFullContact(contact)
            return true
        }

        return false
    }

    private fun addFullContact(contact: Contact) {
        // apply contact to overall scan intersection mask
        if (!contact.intersectionMask.isEmpty)
            intersectionMask.or(contact.intersectionMask)
        else if (contact.isCircle)
            intersectionMask.setRegion(contact.intersectionBounds as Vector3i, true)
        else
            intersectionMask.setRegion(contact.intersectionBounds, true)

        if (contact.contactType !== UNDEFINED_CONTACT_TYPE)
            contactTypes + contact.contactType

        if (contact.materialType != UNDEFINED_MATERIAL)
            materialTypes + contact.materialType

        contacts.add(contact)
    }

}