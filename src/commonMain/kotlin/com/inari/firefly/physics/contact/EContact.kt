package com.inari.firefly.physics.contact

import com.inari.firefly.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.MATERIAL_ASPECT_GROUP
import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.aspect.Aspect
import com.inari.util.geom.BitMask
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class EContact private constructor() : EntityComponent(EContact::class.simpleName!!) {

    @JvmField internal var collisionResolverRef = -1
    @JvmField internal val contactScans = ContactScans()
    @JvmField var notifyContacts = false
    @JvmField val withCollisionResolver = ComponentRefResolver(CollisionResolver) { index -> collisionResolverRef = index }
    @JvmField var isCircle = false
    @JvmField var bounds: Vector4i = Vector4i()
    var mask: BitMask = BitMask(width = 0, height = 0)
        set(value) {
            mask.reset(value.region)
            mask.or(value)
        }
    var material: Aspect  = UNDEFINED_MATERIAL
        set(value) =
            if (MATERIAL_ASPECT_GROUP.typeCheck(value)) field = value
            else throw IllegalArgumentException()

    var contactType: Aspect  = UNDEFINED_CONTACT_TYPE
        set(value) =
            if (CONTACT_TYPE_ASPECT_GROUP.typeCheck(value)) field = value
            else throw IllegalArgumentException()

    @JvmField val withConstraint = ComponentRefResolver(ContactConstraint) { id ->
        if (ContactSystem.constraints[id].fullScan)
            contactScans.registerScan(FullContactScan(id))
        else
            contactScans.registerScan(SimpleContactScan(id))
    }
    @JvmField val removeConstraint = ComponentRefResolver(ContactConstraint) { id: Int ->
        contactScans.removeScan(id)
    }

    fun withConstraint(builder: SystemComponentSingleType<ContactConstraint>, configure: (ContactConstraint.() -> Unit)): CompId {
        val constraint = builder.buildAndGet(configure)
        if (constraint.fullScan)
            contactScans.registerScan(FullContactScan(constraint.index))
        else
            contactScans.registerScan(SimpleContactScan(constraint.index))
        return constraint.componentId
    }

    fun <A : CollisionResolver> withResolver(builder: SystemComponentSubType<CollisionResolver, A>, configure: (A.() -> Unit)): CompId {
        val id = builder.build(configure)
        collisionResolverRef = id.instanceId
        return id
    }

    fun clearConstraints() = contactScans.clear()

    override fun reset() {
        collisionResolverRef = -1
        bounds(0, 0, 0, 0)
        mask.clearMask()
        material = UNDEFINED_MATERIAL
        contactType = UNDEFINED_CONTACT_TYPE
        contactScans.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EContact>(EContact::class) {
        override fun createEmpty() = EContact()
    }
}