package com.inari.firefly.physics.contact

import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.util.aspect.Aspect
import com.inari.util.geom.BitMask
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField


class Contact internal constructor() {

    @JvmField internal var entity = -1
    @JvmField internal val bounds = Vector4i()
    @JvmField internal val intersection = Vector4i()
    @JvmField internal val mask = BitMask(width = 0, height = 0)
    @JvmField internal var contact = UNDEFINED_CONTACT_TYPE
    @JvmField internal var material = UNDEFINED_MATERIAL

    val entityId: Int
        get() = entity
    val worldBounds: Vector4i
        get() = bounds
    val intersectionBounds: Vector4i
        get() = intersection
    val intersectionMask: BitMask
        get() = mask
    val contactType: Aspect
        get() = contact
    val materialType: Aspect
        get() = material

    fun intersects(x: Int, y: Int): Boolean =
        GeomUtils.contains(intersection, x, y)

    fun hasContact(x: Int, y: Int): Boolean {
        if (GeomUtils.contains(intersection, x, y)) {
            if (!mask.isEmpty) {
                return mask.getBit(x - mask.x, y - mask.y)
            }
            return true
        }
        return false
    }

    override fun toString(): String {
        return "Contact(entity=$entity, " +
            "bounds=$bounds, " +
            "intersection=$intersection, " +
            "mask=$mask, " +
            "contact=$contact, " +
            "material=$material)"
    }

    companion object {
        @JvmField val NO_CONTACT = Contact()
    }


}