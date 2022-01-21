package com.inari.firefly.physics.contact

import com.inari.firefly.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.MATERIAL_ASPECT_GROUP
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.graphics.view.Layer
import com.inari.util.aspect.Aspects
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