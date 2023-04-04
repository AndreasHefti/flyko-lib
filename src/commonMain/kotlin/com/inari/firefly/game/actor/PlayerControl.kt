package com.inari.firefly.game.actor

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.physics.movement.Movement.BasicMovementAspect.*
import com.inari.util.ZERO_FLOAT
import kotlin.jvm.JvmField
import kotlin.math.max
import kotlin.math.min

class PlatformerHMoveController private constructor() : SingleComponentControl<Player>(Player) {

    @JvmField var runVelocityStep = 10.0f
    @JvmField var stopVelocityStep = 20.0f
    @JvmField var moveOnAir = true

    @JvmField var inputDevice: InputDevice = Engine.input.getDefaultDevice()
    @JvmField var buttonLeft = ButtonType.LEFT
    @JvmField var buttonRight = ButtonType.RIGHT

    override fun update(component: Player) {
        val mov = component.playerMovement ?: return
        if (!moveOnAir && !mov.onGround)
            return

        if (inputDevice.buttonPressed(buttonLeft)) {
            val outRef = this@PlatformerHMoveController
            if (mov.velocity.v0 <= -mov.maxVelocityWest)
                return

            if (mov.velocity.v0 > ZERO_FLOAT)
                mov.velocity.v0 = max(ZERO_FLOAT, mov.velocity.v0 - outRef.stopVelocityStep)
            else
                mov.velocity.v0 = max( -mov.maxVelocityWest, mov.velocity.v0 - outRef.runVelocityStep)
        } else if (inputDevice.buttonPressed(buttonRight)) {
            val outRef = this@PlatformerHMoveController
            if (mov.velocity.v0 >= mov.maxVelocityWest)
                return
            if (mov.velocity.v0 < ZERO_FLOAT)
                mov.velocity.v0 = min(ZERO_FLOAT, mov.velocity.v0 + outRef.stopVelocityStep)
            else
                mov.velocity.v0 = min(mov.maxVelocityWest, mov.velocity.v0 + outRef.runVelocityStep)
        } else if (mov.velocity.v0 != ZERO_FLOAT) {
            val outRef = this@PlatformerHMoveController
            if (mov.velocity.v0 > ZERO_FLOAT)
                mov.velocity.v0 = max(ZERO_FLOAT, mov.velocity.v0 - outRef.stopVelocityStep)
            else
                mov.velocity.v0 = min(ZERO_FLOAT, mov.velocity.v0 + outRef.stopVelocityStep)
        }
    }

    companion object : SubComponentBuilder<Control, PlatformerHMoveController>(Control) {
        override fun create() = PlatformerHMoveController()
    }
}

class PlatformerJumpController private constructor(): SingleComponentControl<Player>(Player) {

    @JvmField var inputDevice: InputDevice = Engine.input.getDefaultDevice()
    @JvmField var jumpButton = ButtonType.FIRE_1
    @JvmField var jumpImpulse = 100f
    @JvmField var doubleJump = false
    @JvmField var jumpActionTolerance = 5

    private var jumpAction = 0
    private var doubleJumpOn = true

    override fun update(component: Player) {
        val mov = component.playerMovement ?: return

        if (mov.onGround) {
            mov.aspects[JUMP] = false
            mov.aspects[DOUBLE_JUMP] = false
        }

        if (inputDevice.buttonTyped(jumpButton)) {
            if (mov.onGround) {
                mov.onGround = false
                mov.velocity.v1 = -jumpImpulse
                doubleJumpOn = false
                jumpAction = 0
                mov.aspects[JUMP] = true
            } else if (doubleJump && !doubleJumpOn) {
                mov.velocity.v1 = -jumpImpulse
                doubleJumpOn = true
                jumpAction = 0
                mov.aspects[JUMP] = false
                mov.aspects[DOUBLE_JUMP] = true
            } else
                jumpAction = 1

        } else if (mov.onGround && jumpAction > 0 && jumpAction < jumpActionTolerance) {
            mov.onGround = false
            mov.velocity.v1 = -jumpImpulse
            doubleJumpOn = false
            jumpAction = 0
        } else if (jumpAction > 0)
            jumpAction++
    }

    companion object : SubComponentBuilder<Control, PlatformerJumpController>(Control) {
        override fun create() = PlatformerJumpController()
    }
}