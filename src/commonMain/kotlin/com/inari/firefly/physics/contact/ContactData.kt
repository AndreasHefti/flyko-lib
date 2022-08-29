package com.inari.firefly.physics.contact

import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_MATERIAL
import com.inari.util.geom.BitMask
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector3i
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class Contact internal constructor() {

    var entityId = -1
        internal set
    @JvmField var isCircle = false
    @JvmField val worldCircle = Vector3i()
    @JvmField val worldBounds = Vector4i()
    @JvmField val intersectionBounds = Vector4i()
    @JvmField val intersectionMask = BitMask(width = 0, height = 0)
    var contactType = UNDEFINED_CONTACT_TYPE
        internal set
    var materialType = UNDEFINED_MATERIAL
        internal set

    fun intersects(x: Int, y: Int): Boolean =
        GeomUtils.contains(intersectionBounds, x, y)

    fun hasContact(x: Int, y: Int): Boolean {
        if (GeomUtils.contains(intersectionBounds, x, y)) {
            if (!intersectionMask.isEmpty) {
                return intersectionMask.getBit(x - intersectionMask.x, y - intersectionMask.y)
            }
            return true
        }
        return false
    }

    override fun toString(): String {
        return "Contact(entityId=$entityId, worldBounds=$worldBounds, intersectionBounds=$intersectionBounds, intersectionMask=$intersectionMask, contactType=$contactType, materialType=$materialType)"
    }

    companion object {
        @JvmField val NO_CONTACT = Contact()
    }
}


class ContactBounds {

    @JvmField val bounds: Vector4i = Vector4i()
    @JvmField  var bitmask: BitMask? = null

    val isEmpty: Boolean get() = bounds.x == 0 && bounds.y == 0 && bounds.width == 0 && bounds.height == 0 && bitmask == null
    val isCircle: Boolean get() = bounds.height == 0
    val hasContactMask: Boolean get() = bitmask != null

    operator fun invoke(circle: Vector3i) {
        bitmask = null
        bounds(circle.x, circle.y, circle.radius, 0)
    }
    operator fun invoke(cx: Int, cy: Int, radius: Int) {
        bitmask = null
        bounds(cx, cy, radius, 0)
    }
    operator fun invoke(rectangle: Vector4i) {
        bitmask = null
        bounds(rectangle)
    }
    operator fun invoke(rx: Int, ry: Int, width: Int, height: Int) {
        bitmask = null
        bounds(rx, ry, width, height)
    }
    operator fun invoke(mask: BitMask?) {
        if (mask != null)
            bounds(mask.region)
        bitmask = mask
    }

    fun clear() {
        bounds(0, 0, 0, 0)
        bitmask = null
    }
}

internal class SystemContactBounds (
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