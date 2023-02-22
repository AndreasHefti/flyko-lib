package com.inari.firefly.game.actor

import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import kotlin.jvm.JvmField

class EProjectile private constructor() : EntityComponent(EProjectile) {

    var type: Aspect = UNDEFINED_PROJECTILE_TYPE
        set(value) =
            if (PROJECTILE_TYPE_ASPECT_GROUP.typeCheck(value)) field = value
            else throw IllegalArgumentException()
    @JvmField var hitPower: Int = 0

    override fun reset() {
        type = UNDEFINED_PROJECTILE_TYPE
        hitPower = 0
    }

    override val componentType = EProjectile
    companion object : EntityComponentBuilder<EProjectile>("EProjectile") {
        override fun create() = EProjectile()

        @JvmField val PROJECTILE_TYPE_ASPECT_GROUP = IndexedAspectType("PROJECTILE_TYPE_ASPECT_GROUP")
        @JvmField val UNDEFINED_PROJECTILE_TYPE = PROJECTILE_TYPE_ASPECT_GROUP.createAspect("UNDEFINED_PROJECTILE_TYPE")
    }
}