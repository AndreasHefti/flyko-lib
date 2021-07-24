package com.inari.firefly.actor

import com.inari.firefly.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.PROJECTILE_TYPE_ASPECT
import com.inari.firefly.UNDEFINED_PROJECTILE_TYPE
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.aspect.Aspect
import kotlin.jvm.JvmField

class EProjectile private constructor () : EntityComponent(EProjectile::class.simpleName!!) {

    var type: Aspect = UNDEFINED_PROJECTILE_TYPE
        set(value) =
            if (PROJECTILE_TYPE_ASPECT.typeCheck(value)) field = value
            else throw IllegalArgumentException()
    var hitPower: Int = 0

    override fun reset() {
        type = UNDEFINED_PROJECTILE_TYPE
        hitPower = 0
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EProjectile>(EProjectile::class) {
        @JvmField val PROJECTILE_CONTACT_TYPE = CONTACT_TYPE_ASPECT_GROUP.createAspect("PROJECTILE_CONTACT_TYPE")
        override fun createEmpty() = EProjectile()
    }
}