package com.inari.firefly.physics.contact

import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.core.component.CompId
import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspect
import com.inari.util.geom.BitMask
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector3i
import com.inari.util.geom.Vector4i
import com.inari.util.indexed.Indexed
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