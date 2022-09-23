package com.inari.firefly.physics.contact

import com.inari.firefly.core.*
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import kotlin.jvm.JvmField

class EContact private constructor() : EntityComponent(EContact) {

    @JvmField var collisionResolverRef = CReference(CollisionResolver)
    @JvmField internal val contactScans = ContactScans()
    @JvmField var notifyContacts = false
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
    fun removeConstraint(index: Int) = removeConstraint(ContactConstraint.getKey(index))
    fun removeConstraint(key: ComponentKey) {
        if (key.type != ContactConstraint)
            throw IllegalArgumentException("Type mismatch: $key.type")
        contactScans.removeScan(index)
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
        //bounds(0, 0, 0, 0)
        //mask.clearMask()
        material = UNDEFINED_MATERIAL
        contactType = UNDEFINED_CONTACT_TYPE
        contactScans.clear()
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EContact>("EContact") {
        override fun create() = EContact()

        @JvmField val MATERIAL_ASPECT_GROUP = IndexedAspectType("MATERIAL_ASPECT_GROUP")
        @JvmField val UNDEFINED_MATERIAL: Aspect = MATERIAL_ASPECT_GROUP.createAspect("UNDEFINED_MATERIAL")

        @JvmField val CONTACT_TYPE_ASPECT_GROUP = IndexedAspectType("CONTACT_TYPE_ASPECT_GROUP")
        @JvmField val UNDEFINED_CONTACT_TYPE: Aspect = CONTACT_TYPE_ASPECT_GROUP.createAspect("UNDEFINED_CONTACT_TYPE")
    }
}