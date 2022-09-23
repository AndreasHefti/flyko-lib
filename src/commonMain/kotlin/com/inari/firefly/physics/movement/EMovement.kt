package com.inari.firefly.physics.movement

import com.inari.firefly.FFContext
import com.inari.firefly.INFINITE_SCHEDULER
import com.inari.firefly.MOVEMENT_ASPECT_GROUP
import com.inari.firefly.ZERO_FLOAT
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.aspect.Aspects
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class EMovement private constructor() : EntityComponent(EMovement::class.simpleName!!) {

    @JvmField internal var integratorRef = -1
    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER

    @JvmField var active = true
    @JvmField val integrator = ComponentRefResolver(Integrator) { index-> integratorRef = index }
    @JvmField var mass = 1f
    @JvmField val force = Vector2f(0f, 0f)
    @JvmField val acceleration = Vector2f(0f, 0f)
    @JvmField val velocity = Vector2f(0f, 0f)

    @JvmField var maxVelocityNorth = Float.MAX_VALUE
    @JvmField var maxVelocityEast = Float.MAX_VALUE
    @JvmField var maxVelocitySouth = Float.MAX_VALUE
    @JvmField var maxVelocityWest = Float.MAX_VALUE

    @JvmField var onGround = false

    @JvmField val aspects: Aspects = MOVEMENT_ASPECT_GROUP.createAspects()

    var velocityX: Float
        get() = velocity.v0
        set(value) { velocity.v0 = value }
    var velocityY: Float
        get() = velocity.v1
        set(value) { velocity.v1 = value }
    var accelerationX: Float
        get() = acceleration.v0
        set(value) { acceleration.v0 = value }
    var accelerationY: Float
        get() = acceleration.v1
        set(value) { acceleration.v1 = value }

    var updateResolution: Float
        get() = throw UnsupportedOperationException()
        set(value) { scheduler = FFContext.timer.createUpdateScheduler(value) }

    fun <A : Integrator> withIntegrator(builder: SystemComponentSubType<Integrator, A>, configure: (A.() -> Unit)): CompId {
        if (initialized)
            throw IllegalStateException("EMovement instance is already created")
        val id = builder.build(configure)
        integratorRef = id.instanceId
        return id
    }

    fun <A : Integrator> withActiveIntegrator(builder: SystemComponentSubType<Integrator, A>, configure: (A.() -> Unit)): CompId {
        if (initialized)
            throw IllegalStateException("EMovement instance is already created")
        val id = builder.buildAndActivate(configure)
        integratorRef = id.instanceId
        return id
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

    override fun componentType() = Companion
    companion object : EntityComponentType<EMovement>(EMovement::class) {
        override fun createEmpty() = EMovement()
    }
}