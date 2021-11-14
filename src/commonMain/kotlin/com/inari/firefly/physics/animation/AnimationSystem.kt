package com.inari.firefly.physics.animation

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object AnimationSystem : ComponentSystem {
    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Animation)

    @JvmField val animations = ComponentSystem.createComponentMapping(
        Animation,
        nameMapping = true,
        activationMapping = true
    )

    private val entityActivationListener: EntityEventListener = object : EntityEventListener {
        override fun entityActivated(entity: Entity) {
            val animation = entity[EAnimation]
            val i = animation.animations.iterator()
            while (i.hasNext()) {
                val data = i.next()
                data.init(entity.componentId)
                animations[data.animationRef].register(data)
            }
        }
        override fun entityDeactivated(entity: Entity) {
            val animation = entity[EAnimation]
            val i = animation.animations.iterator()
            while (i.hasNext()) {
                val data = i.next()
                animations[data.animationRef].dispose(data)
            }
        }
        override fun match(aspects: Aspects): Boolean =
            aspects.contains(EAnimation)
    }

    init {
        FFContext.loadSystem(this)
        FFContext.registerListener(FFApp.UpdateEvent, this::update)
        FFContext.registerListener(EntityEvent, entityActivationListener)
    }
    
    private fun update() = animations.forEachActive { it.update() }

    override fun clearSystem() =
        animations.clear()
}