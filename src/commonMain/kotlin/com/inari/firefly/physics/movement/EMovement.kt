package com.inari.firefly.physics.movement

import com.inari.firefly.FFContext
import com.inari.firefly.INFINITE_SCHEDULER
import com.inari.firefly.control.Controller
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.physics.animation.Animation
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class EMovement private constructor() : EntityComponent(EMovement::class.simpleName!!) {

    @JvmField internal var integratorRef = -1
    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER

    var active: Boolean = true
    val integrator = ComponentRefResolver(Integrator) { index-> integratorRef = index }
    val velocity: Vector2f  = Vector2f(0f, 0f)
    var velocityX: Float
        get() = velocity.dx
        set(value) { velocity.dx = value }
    var velocityY: Float
        get() = velocity.dy
        set(value) { velocity.dy = value }
    val acceleration: Vector2f = Vector2f(0f, 0f)
    var accelerationX: Float
        get() = acceleration.dx
        set(value) { acceleration.dx = value }
    var accelerationY: Float
        get() = acceleration.dy
        set(value) { acceleration.dy = value }
    var mass: Float = 0f
    var massFactor: Float  = 1f
    var maxGravityVelocity: Float = 1f
    var onGround: Boolean  = false
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
        velocity.dx = 0f
        velocity.dy = 0f
        acceleration.dx = 0f
        acceleration.dy = 0f
        mass = 0f
        massFactor = 1f
        maxGravityVelocity = 200f
        onGround = false
        scheduler = INFINITE_SCHEDULER
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EMovement>(EMovement::class) {
        override fun createEmpty() = EMovement()
    }
}