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
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object Movement : SystemControl(Entity) {

    @JvmField val MOVEMENT_ASPECT_GROUP = IndexedAspectType("MOVEMENT_ASPECT_GROUP")
    @JvmField val UNDEFINED_MOVEMENT = MOVEMENT_ASPECT_GROUP.createAspect("UNDEFINED_MOVEMENT")

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

    val moveEventType = Event.EventType("MoveEvent")
    val moveEvent = MoveEvent(moveEventType)
    private var deltaTimeInSeconds: Float = 0.0f

    init {
        Control.registerAsSingleton(this, true)
        Control.activate(this.index)
    }

    override fun matchForControl(key: ComponentKey) =
        EMovement in Entity[key].aspects


    override fun update() {
        moveEvent.entities.clear()
        deltaTimeInSeconds = min(Engine.timer.timeElapsed / 1000f, .5f)

        super.update()

        if (!moveEvent.entities.isEmpty)
            Engine.notify(moveEvent)
    }

    override fun update(index: Int) {
        val entity = Entity[index]

        val movement = entity[EMovement]
        if (!movement.active || !movement.scheduler.needsUpdate())
            return

        val dtEntity = deltaTimeInSeconds * (60 / movement.scheduler.resolution)

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

