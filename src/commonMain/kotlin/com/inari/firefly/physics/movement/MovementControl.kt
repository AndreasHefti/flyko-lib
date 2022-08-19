package com.inari.firefly.physics.movement

import com.inari.firefly.core.*
import com.inari.firefly.graphics.view.ETransform
import com.inari.util.ZERO_FLOAT

import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.collection.BitSet
import com.inari.util.event.Event
import kotlin.jvm.JvmField
import kotlin.math.min


object MovementControl : ComponentControl<Entity>() {

    @JvmField val MOVEMENT_ASPECT_GROUP = IndexedAspectType("MOVEMENT_ASPECT_GROUP")
    @JvmField val UNDEFINED_MOVEMENT = MOVEMENT_ASPECT_GROUP.createAspect("UNDEFINED_MOVEMEN")

    enum class MovementAspect(private val aspect: Aspect) : Aspect {
        ON_GROUND(MOVEMENT_ASPECT_GROUP.createAspect("ON_GROUND")),
        BLOCK_WEST(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_WEST")),
        BLOCK_EAST(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_EAST")),
        BLOCK_NORTH(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_NORTH")),
        BLOCK_SOUTH(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_SOUTH")),
        ;
        override val aspectIndex: Int get() = aspect.aspectIndex
        override val aspectName: String get() = aspect.aspectName
        override val aspectType: AspectType get() = aspect.aspectType
    }

    init { Control.registerAsSingleton(this) }

    override val controlledComponentType = Entity

    private val entities: BitSet = BitSet()

    override fun notifyActivation (component: Entity) {
        if (EMovement !in component.components) return
        entities[component.index] = true
    }
    override fun notifyDeactivation(component: Entity) {
        if (EMovement !in component.components) return
        entities[component.index] = false
    }

    override fun update() {
        MoveEvent.moveEvent.entities.clear()
        val deltaTimeInSeconds: Float = min(Engine.timer.timeElapsed / 1000f, .5f)

        var i: Int = entities.nextSetBit(0)
        while (i >= 0) {
            val entity = Entity[i]
            i = entities.nextSetBit(i + 1)

            val movement = entity[EMovement]
            if (!movement.active || !movement.scheduler.needsUpdate())
                continue

            val dtEntity = deltaTimeInSeconds * (60 / movement.scheduler.resolution)

            val transform = entity[ETransform]
            movement.integrator.integrate(movement, transform, dtEntity)
            if (movement.velocity.v0 != ZERO_FLOAT || movement.velocity.v1 != ZERO_FLOAT) {
                movement.integrator.step(movement, transform, dtEntity)
                MoveEvent.moveEvent.entities.set(entity.index)
            }
        }

        if (!MoveEvent.moveEvent.entities.isEmpty)
            Engine.notify(MoveEvent.moveEvent)
    }

    class MoveEvent(override val eventType: EventType) : Event<(MoveEvent) -> Unit>() {

        @JvmField val entities: BitSet = BitSet(100)

        override fun notify(listener: (MoveEvent) -> Unit) = listener(this)

        companion object : EventType("MoveEvent") {
            internal val moveEvent = MoveEvent(this)
        }
    }
}

