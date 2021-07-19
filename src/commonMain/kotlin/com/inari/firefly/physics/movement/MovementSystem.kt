package com.inari.firefly.physics.movement

import com.inari.firefly.FFApp.UpdateEvent
import com.inari.firefly.FFContext
import com.inari.firefly.core.system.FFSystem
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityActivationEvent
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet

object MovementSystem : FFSystem {

    var movementIntegrator: Integrator = SimpleStepIntegrator

    private val entities: BitSet = BitSet()

    init {
        FFContext.registerListener(UpdateEvent) {
            MoveEvent.moveEvent.entities.clear()
            val deltaTimeInSeconds: Long = FFContext.timer.timeElapsed / 1000

            var i: Int = entities.nextSetBit(0)
            while (i >= 0) {
                val entity = EntitySystem[i]
                i = entities.nextSetBit(i + 1)

                val movement = entity[EMovement]
                if (!movement.active || !movement.scheduler.needsUpdate())
                    continue

                val transform = entity[ETransform]
                if (movement.velocity.dx != 0f || movement.velocity.dy != 0f) {
                    movementIntegrator.step(movement, transform, deltaTimeInSeconds)
                    MoveEvent.moveEvent.entities.set(entity.index)
                }

                movementIntegrator.integrate(movement, transform, deltaTimeInSeconds)
            }

            if (!MoveEvent.moveEvent.entities.isEmpty)
                FFContext.notify(MoveEvent.moveEvent)
        }


        FFContext.registerListener(EntityActivationEvent, object : EntityActivationEvent.Listener {
            override fun entityActivated(entity: Entity) {
                entities.set(entity.index)
            }
            override fun entityDeactivated(entity: Entity) {
                entities.set(entity.index, false)
            }
            override fun match(aspects: Aspects): Boolean =
                aspects.contains(EMovement)
        })
    }

    override fun clearSystem() {}
}