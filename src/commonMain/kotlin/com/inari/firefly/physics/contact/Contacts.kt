package com.inari.firefly.physics.contact

import com.inari.firefly.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.MATERIAL_ASPECT_GROUP
import com.inari.firefly.physics.contact.Contact.Companion.NO_CONTACT
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.*
import kotlin.jvm.JvmField
import kotlin.math.ceil
import kotlin.math.floor


class Contacts internal constructor(
    @JvmField internal var constraintRef: Int
) {

    @JvmField internal val normalizedContactBounds = Rectangle()
    @JvmField internal val worldBounds = Rectangle()
    @JvmField internal val contactTypes = CONTACT_TYPE_ASPECT_GROUP.createAspects()
    @JvmField internal val materialTypes = MATERIAL_ASPECT_GROUP.createAspects()
    @JvmField internal val intersectionMask = BitMask(width = 0, height = 0)
    @JvmField internal val contacts: DynArray<Contact> = DynArray.of(5, 5)

    internal fun update(contactBounds: Rectangle, position: PositionF, velocity: Vector2f) {
        clear()

        normalizedContactBounds.width = contactBounds.width
        normalizedContactBounds.height = contactBounds.height

        worldBounds.x = (if (velocity.dx > 0) ceil(position.x.toDouble()).toInt() else floor(position.x.toDouble()).toInt()) + contactBounds.x
        worldBounds.y = (if (velocity.dy > 0) ceil(position.y.toDouble()).toInt() else floor(position.y.toDouble()).toInt()) + contactBounds.y
        worldBounds.width = contactBounds.width
        worldBounds.height = contactBounds.height
        intersectionMask.reset(0, 0, contactBounds.width, contactBounds.height)
    }

    val width: Int
        get() = normalizedContactBounds.width
    val height: Int
        get() = normalizedContactBounds.height
    val contactMask: BitMask
        get() = intersectionMask

    fun hasAnyContact(): Boolean =
        !contacts.isEmpty

    fun hasContact(contact: Aspects): Boolean =
        contactTypes.intersects(contact)

    fun hasContact(contact: Aspect): Boolean =
        contactTypes.contains(contact)

    fun hasAnyMaterialContact(materials: Aspects): Boolean =
        materialTypes.intersects(materials)

    fun hasMaterialContact(material: Aspect): Boolean  =
        materialTypes.contains(material)

    fun allContacts(): DynArrayRO<Contact> =
        contacts

    fun hasContact(p: Position): Boolean =
        intersectionMask.getBit(p.x, p.y)

    fun hasContact(p1: Position, p2: Position): Boolean =
        intersectionMask.getBit(p1.x, p1.y) || intersectionMask.getBit(p2.x, p2.y)

    fun hasContact(x: Int, y: Int): Boolean =
        intersectionMask.getBit(x, y)

    fun hasContactType(contactType: Aspect, p: Position): Boolean =
        hasContactType(contactType, p.x, p.y)

    fun hasContactType(contactType: Aspect, x: Int, y: Int): Boolean {
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

    fun hasContactTypeExclusive(contactType: Aspect, p: Position): Boolean =
        hasContactTypeExclusive(contactType, p.x, p.y)

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

    fun hasContact(material: Aspect, p: Position): Boolean =
        hasContact(material, p.x, p.y)

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

    fun hasContactExclusive(material: Aspect, p: Position): Boolean =
        hasContactExclusive(material, p.x, p.y)

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

    fun get(pos: Position): Contact = this[pos.x, pos.y]

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

    fun clear() {
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

    override fun toString(): String {
        return "Contacts(constraint=$constraintRef, " +
            "normalizedContactBounds=$normalizedContactBounds, " +
            "worldBounds=$worldBounds, " +
            "contactTypes=$contactTypes, " +
            "materialTypes=$materialTypes, " +
            "intersectionMask=$intersectionMask, " +
            "contacts=$contacts)"
    }

}