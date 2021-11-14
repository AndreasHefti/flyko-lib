package com.inari.firefly.physics.movement

import com.inari.firefly.FFApp.UpdateEvent
import com.inari.firefly.FFContext
import com.inari.firefly.ZERO_FLOAT
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField
import kotlin.math.min

object MovementSystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
        Integrator
    )

    @JvmField val integrator = ComponentSystem.createComponentMapping(
        Integrator,
        activationMapping = true,
        nameMapping = true
    )

    @JvmField val defaultIntegrator = SimpleStepIntegrator.buildAndGet {}

    private val entities: BitSet = BitSet()

    private val entityActivationListener: EntityEventListener = object : EntityEventListener {
        override fun entityActivated(entity: Entity) {
            entities[entity.index] = true
        }
        override fun entityDeactivated(entity: Entity) {
            entities[entity.index] = false
        }
        override fun match(aspects: Aspects): Boolean =
            aspects.contains(EMovement)
    }

    init {
        FFContext.registerListener(UpdateEvent, this::processMove)
        FFContext.registerListener(EntityEvent, entityActivationListener)
        FFContext.loadSystem(this)
    }

    private fun processMove() {
        MoveEvent.moveEvent.entities.clear()
        val deltaTimeInSeconds: Float = min(FFContext.timer.timeElapsed / 1000f, .5f)

        var i: Int = entities.nextSetBit(0)
        while (i >= 0) {
            val entity = EntitySystem[i]
            i = entities.nextSetBit(i + 1)

            val movement = entity[EMovement]
            if (!movement.active || !movement.scheduler.needsUpdate())
                continue

            val movementIntegrator = if (movement.integratorRef < 0)
                defaultIntegrator
            else
                integrator[movement.integratorRef]
            val transform = entity[ETransform]
            movementIntegrator.integrate(movement, transform, deltaTimeInSeconds)
            if (movement.velocity.dx != ZERO_FLOAT || movement.velocity.dy != ZERO_FLOAT) {
                movementIntegrator.step(movement, transform, deltaTimeInSeconds)
                MoveEvent.moveEvent.entities.set(entity.index)
            }
        }

        if (!MoveEvent.moveEvent.entities.isEmpty)
            FFContext.notify(MoveEvent.moveEvent)
    }

    override fun clearSystem() {
        integrator.clear()
        entities.clear()
    }
}