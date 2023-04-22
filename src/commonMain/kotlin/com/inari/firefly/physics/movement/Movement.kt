package com.inari.firefly.physics.movement

import com.inari.firefly.core.Control
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.EntityControl
import com.inari.firefly.graphics.view.ETransform
import com.inari.util.ZERO_FLOAT
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.collection.BitSet
import com.inari.util.event.Event
import kotlin.jvm.JvmField
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object Movement : EntityControl() {

    @JvmField val MOVEMENT_ASPECT_GROUP = IndexedAspectType("MOVEMENT_ASPECT_GROUP")
    @JvmField val UNDEFINED_MOVEMENT = MOVEMENT_ASPECT_GROUP.createAspect("UNDEFINED_MOVEMENT")

    enum class BasicMovementAspect(private val aspect: Aspect) : Aspect {
        ON_SLOPE_UP(MOVEMENT_ASPECT_GROUP.createAspect("ON_SLOPE_UP")),
        ON_SLOPE_DOWN(MOVEMENT_ASPECT_GROUP.createAspect("ON_SLOPE_DOWN")),
        GROUND_TOUCHED(MOVEMENT_ASPECT_GROUP.createAspect("GROUND_TOUCHED")),
        GROUND_LOOSE (MOVEMENT_ASPECT_GROUP.createAspect("GROUND_LOOSE")),
        SLIP_RIGHT (MOVEMENT_ASPECT_GROUP.createAspect("SLIP_RIGHT")),
        SLIP_LEFT (MOVEMENT_ASPECT_GROUP.createAspect("SLIP_RIGHT")),
        JUMP(MOVEMENT_ASPECT_GROUP.createAspect("JUMP")),
        DOUBLE_JUMP(MOVEMENT_ASPECT_GROUP.createAspect("DOUBLE_JUMP")),
        CLIMB_UP(MOVEMENT_ASPECT_GROUP.createAspect("CLIMB_UP")),
        CLIMB_DOWN(MOVEMENT_ASPECT_GROUP.createAspect("CLIMB_DOWN")),
        BLOCK_WEST(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_WEST")),
        BLOCK_EAST(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_EAST")),
        BLOCK_NORTH(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_NORTH")),
        BLOCK_SOUTH(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_SOUTH")),
        ;
        override val aspectIndex: Int get() = aspect.aspectIndex
        override val aspectName: String get() = aspect.aspectName
        override val aspectType: AspectType get() = aspect.aspectType
    }

    val moveEventType = Event.EventType("MoveEvent")
    val moveEvent = MoveEvent(moveEventType)
    private var deltaTimeInSeconds: Float = 0.0f

    init {
        @Suppress("LeakingThis") registerStatic(this)
        Control.activate(this.index)
    }

    override fun matchForControl(entity: Entity) =
        EMovement in entity.aspects


    override fun update() {

        moveEvent.entities.clear()
        deltaTimeInSeconds = min(Engine.timer.timeElapsed / 1000f, .5f)

        super.update()

        if (!moveEvent.entities.isEmpty)
            Engine.notify(moveEvent)
    }

    override fun update(entity: Entity) {
        val movement = entity[EMovement]
        if (!movement.active || !movement.scheduler.needsUpdate())
            return

        val dtEntity = deltaTimeInSeconds * (60f / min(60f, movement.scheduler.resolution))

        val transform = entity[ETransform]
        movement.integrator.integrate(movement, transform, dtEntity)
        if (movement.velocity.v0 != ZERO_FLOAT || movement.velocity.v1 != ZERO_FLOAT) {
            movement.integrator.step(movement, transform, dtEntity)
            moveEvent.entities.set(entity.index)
        }
    }

    class MoveEvent(override val eventType: EventType) : Event<(MoveEvent) -> Unit>() {
        @JvmField val entities: BitSet = BitSet(100)
        override fun notify(listener: (MoveEvent) -> Unit) = listener(this)
    }
}

