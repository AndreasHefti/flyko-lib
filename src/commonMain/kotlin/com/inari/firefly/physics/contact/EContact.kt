package com.inari.firefly.physics.contact

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ComponentIndex
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class EContact private constructor() : EntityComponent(EContact) {

    @JvmField internal val contactScans = ContactScans()
    @JvmField internal val contactCallbacks = DynArray.of<ContactCallbackConstraint>()

    @JvmField var collisionResolverRef = CReference(CollisionResolver)
    @JvmField val contactConstraintRef = CReference(ContactConstraint)
    @JvmField val contactBounds = ContactBounds()
    val isCircle get() = contactBounds.isCircle
    val hasContactMask get() = contactBounds.hasContactMask
    var material: Aspect  = UNDEFINED_MATERIAL
        set(value) =
            if (MATERIAL_ASPECT_GROUP.typeCheck(value)) field = value
            else throw IllegalArgumentException()

    var contactType: Aspect  = UNDEFINED_CONTACT_TYPE
        set(value) =
            if (CONTACT_TYPE_ASPECT_GROUP.typeCheck(value)) field = value
            else throw IllegalArgumentException()

    fun withConstraint(configure: ContactConstraint.() -> Unit): ComponentKey {
        val cc = ContactConstraint.buildAndGet(configure)
        withConstraint(cc)
        val key = ContactConstraint.getKey(cc.index)
        ContactConstraint.activate(key)
        return key
    }

    fun withConstraint(contactConstraint: ContactConstraint) {
        if (contactConstraint.fullScan)
            contactScans.registerScan(FullContactScan(contactConstraint.index))
        else
            contactScans.registerScan(SimpleContactScan(contactConstraint.index))
    }

    fun removeConstraint(constraint: ContactConstraint) = removeConstraint(constraint.index)
    fun removeConstraint(name: String) = removeConstraint(ContactConstraint.getKey(name))
    fun removeConstraint(index: ComponentIndex) = removeConstraint(ContactConstraint.getKey(index))
    fun removeConstraint(key: ComponentKey) {
        if (key.type != ContactConstraint)
            throw IllegalArgumentException("Type mismatch: $key.type")
        contactScans.removeScan(index)
    }

    fun withContactCallbackConstraint(builder: ContactCallbackConstraint.() -> Unit) {
        val contactCallback = ContactCallbackConstraint()
        contactCallback.also(builder)
        contactCallbacks.add(contactCallback)
    }

    fun <A : CollisionResolver> withResolver(builder: ComponentBuilder<A>, configure: (A.() -> Unit)): ComponentKey {
        val id = builder.build(configure)
        collisionResolverRef(id)
        return id
    }

    fun clearConstraints() = contactScans.clear()

    override fun reset() {
        collisionResolverRef.reset()
        contactBounds.clear()
        material = UNDEFINED_MATERIAL
        contactType = UNDEFINED_CONTACT_TYPE
        contactScans.clear()
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EContact>("EContact") {
        override fun allocateArray() = DynArray.of<EContact>()
        override fun create() = EContact()

        @JvmField val MATERIAL_ASPECT_GROUP = IndexedAspectType("MATERIAL_ASPECT_GROUP")
        @JvmField val UNDEFINED_MATERIAL: Aspect = MATERIAL_ASPECT_GROUP.createAspect("UNDEFINED_MATERIAL")

        @JvmField val CONTACT_TYPE_ASPECT_GROUP = IndexedAspectType("CONTACT_TYPE_ASPECT_GROUP")
        @JvmField val UNDEFINED_CONTACT_TYPE: Aspect = CONTACT_TYPE_ASPECT_GROUP.createAspect("UNDEFINED_CONTACT_TYPE")
    }
}