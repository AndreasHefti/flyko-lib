package com.inari.firefly.physics.movement

import com.inari.firefly.core.Engine
import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.physics.movement.Movement.MOVEMENT_ASPECT_GROUP
import com.inari.util.ZERO_FLOAT
import com.inari.util.aspect.Aspects
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField


class EMovement private constructor() : EntityComponent(EMovement) {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    @JvmField var integrator: Integrator = SimpleStepIntegrator.create()

    @JvmField var active = true
    @JvmField var mass = ZERO_FLOAT
    @JvmField val force = Vector2f(0f, 0f)
    @JvmField val acceleration = Vector2f(0f, 0f)
    @JvmField val velocity = Vector2f(0f, 0f)

    @JvmField var maxVelocityNorth = Float.MAX_VALUE
    @JvmField var maxVelocityEast = Float.MAX_VALUE
    @JvmField var maxVelocitySouth = Float.MAX_VALUE
    @JvmField var maxVelocityWest = Float.MAX_VALUE

    @JvmField var onGround = false
    @JvmField val aspects: Aspects = MOVEMENT_ASPECT_GROUP.createAspects()

    var updateResolution: Float
        get() = throw UnsupportedOperationException()
        set(value) { scheduler = Engine.timer.createUpdateScheduler(value) }

    fun <I : Integrator> withIntegrator(builder: IntegratorBuilder<I>, configure: I.() -> Unit) {
        val integrator = builder.create()
        integrator.also(configure)
        this.integrator = integrator
    }

    override fun reset() {
        active = false
        velocity.v0 = 0f
        velocity.v1 = 0f
        acceleration.v0 = 0f
        acceleration.v1 = 0f
        mass = 0f
        scheduler = INFINITE_SCHEDULER
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EMovement>("EMovement") {
        init { Movement } // make sure MovementControl is initialised
        override fun create() = EMovement()
    }
}