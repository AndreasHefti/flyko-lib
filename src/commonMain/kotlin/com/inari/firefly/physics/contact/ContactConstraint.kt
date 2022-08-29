package com.inari.firefly.physics.contact

import com.inari.firefly.core.CReference
import com.inari.firefly.core.Component
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.graphics.view.Layer
import com.inari.util.aspect.Aspects
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class ContactConstraint private constructor(): Component(ContactConstraint) {

    @JvmField var layerRef = CReference(Layer)
    val layerIndex: Int
        get() = layerRef.targetKey.instanceIndex
    @JvmField var fullScan = false
    @JvmField var isCircle = false
    @JvmField val bounds: Vector4i = Vector4i()
    @JvmField val materialFilter: Aspects = EContact.MATERIAL_ASPECT_GROUP.createAspects()
    @JvmField val typeFilter: Aspects = EContact.CONTACT_TYPE_ASPECT_GROUP.createAspects()

    val isFiltering: Boolean
        get() = !materialFilter.isEmpty

    fun match(contact: EContact): Boolean =
        (typeFilter.isEmpty || contact.contactType in typeFilter) &&
                (materialFilter.isEmpty || contact.material in materialFilter)

    override val componentType = Companion
    companion object : ComponentSystem<ContactConstraint>("ContactConstraint") {
        override fun allocateArray(size: Int): Array<ContactConstraint?> = arrayOfNulls(size)
        override fun create() = ContactConstraint()
    }
}