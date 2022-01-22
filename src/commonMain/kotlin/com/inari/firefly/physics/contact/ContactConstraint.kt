package com.inari.firefly.physics.contact

import com.inari.firefly.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.MATERIAL_ASPECT_GROUP
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.graphics.view.Layer
import com.inari.util.aspect.Aspects
import com.inari.util.geom.BitMask
import com.inari.util.geom.Vector3i
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class ContactConstraint private constructor() : SystemComponent(ContactConstraint::class.simpleName!!) {

    @JvmField internal var layerRef = -1
    @JvmField val layer = ComponentRefResolver(Layer) { index->
        layerRef = setIfNotInitialized(index, "Layer")
    }
    @JvmField var fullScan = false
    @JvmField var isCircle = false
    @JvmField val bounds: Vector4i = Vector4i()
    @JvmField val materialFilter: Aspects = MATERIAL_ASPECT_GROUP.createAspects()
    @JvmField val typeFilter: Aspects = CONTACT_TYPE_ASPECT_GROUP.createAspects()

    val isFiltering: Boolean
        get() = !materialFilter.isEmpty

    fun match(contact: EContact): Boolean =
            (typeFilter.isEmpty || contact.contactType in typeFilter) &&
                    (materialFilter.isEmpty || contact.material in materialFilter)

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<ContactConstraint>(ContactConstraint::class) {
        override fun createEmpty() = ContactConstraint()
    }
}

// TODO better handling of empty circle and BitMask plus clear function
// TODO check if this also can be used for EContact and ContactConstraint
class ContactBounds (
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