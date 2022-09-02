package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.VOID_MOVE_CALLBACK
import com.inari.util.ZERO_FLOAT
import kotlin.jvm.JvmField
import kotlin.math.max
import kotlin.math.min

class PlatformerHMoveController private constructor() : SingleComponentControl(Player) {

    @JvmField var runVelocityStep = 10.0f
    @JvmField var stopVelocityStep = 20.0f
    @JvmField var moveOnAir = true

    @JvmField var inputDevice: InputDevice = Engine.input.getDefaultDevice()
    @JvmField var buttonLeft = ButtonType.LEFT
    @JvmField var buttonRight = ButtonType.RIGHT

    @JvmField var directionChangeCallback = VOID_MOVE_CALLBACK

    private lateinit var playerMovement: EMovement

    override fun register(name: String) {
        super.register(name)
        playerMovement = Entity[name][EMovement]
    }

    override fun unregister(name: String) {
        playerMovement = null!!
    }

    override fun update() {
        if (!moveOnAir &&!playerMovement.onGround)
            return

        if (inputDevice.buttonPressed(buttonLeft)) {
                val outRef = this@PlatformerHMoveController
                if (playerMovement.velocity.v0 <= -playerMovement.maxVelocityWest)
                    return

                if (playerMovement.velocity.v0 > ZERO_FLOAT) {
                    playerMovement.velocity.v0 = max(ZERO_FLOAT, playerMovement.velocity.v0 - outRef.stopVelocityStep)
                    outRef.directionChangeCallback(controlledComponentIndex, playerMovement.velocity.v0, outRef.buttonLeft)
                }
                else
                    playerMovement.velocity.v0 = max( -playerMovement.maxVelocityWest, playerMovement.velocity.v0 - outRef.runVelocityStep)
        } else if (inputDevice.buttonPressed(buttonRight)) {
                val outRef = this@PlatformerHMoveController
                if (playerMovement.velocity.v0 >= playerMovement.maxVelocityWest)
                    return
                if (playerMovement.velocity.v0 < ZERO_FLOAT) {
                    playerMovement.velocity.v0 = min(ZERO_FLOAT, playerMovement.velocity.v0 + outRef.stopVelocityStep)
                    outRef.directionChangeCallback(controlledComponentIndex, playerMovement.velocity.v0, outRef.buttonRight)
                }
                else
                    playerMovement.velocity.v0 = min(playerMovement.maxVelocityWest, playerMovement.velocity.v0 + outRef.runVelocityStep)
        } else if (playerMovement.velocity.v0 != ZERO_FLOAT) {
            with (playerMovement) {
                val outRef = this@PlatformerHMoveController
                if (velocity.v0 > ZERO_FLOAT)
                    velocity.v0 = max(ZERO_FLOAT, velocity.v0 - outRef.stopVelocityStep)
                else
                    velocity.v0 = min(ZERO_FLOAT, velocity.v0 + outRef.stopVelocityStep)
            }
        }
    }

    companion object :  ComponentSubTypeSystem<Control, PlatformerHMoveController>(Control, "PlatformerHMoveController") {
        override fun create() = PlatformerHMoveController()
    }
}

class PlatformerJumpController private constructor(): SingleComponentControl(Player) {

    @JvmField var inputDevice: InputDevice = Engine.input.getDefaultDevice()
    @JvmField var jumpButton = ButtonType.FIRE_1
    @JvmField var jumpImpulse = 100f
    @JvmField var doubleJump = false
    @JvmField var jumpActionTolerance = 5

    private var jumpAction = 0
    private var doubleJumpOn = true
    private lateinit var playerMovement: EMovement

    override fun register(name: String) {
        super.register(name)
        playerMovement = Entity[name][EMovement]
    }

    override fun unregister(name: String) {
        playerMovement = null!!
    }

    override fun update() {
        if (inputDevice.buttonTyped(jumpButton)) {
            if (playerMovement.onGround) {
                playerMovement.onGround = false
                playerMovement.velocity.v1 = -jumpImpulse
                doubleJumpOn = false
                jumpAction = 0
            } else if (doubleJump && !doubleJumpOn) {
                playerMovement.velocity.v1 = -jumpImpulse
                doubleJumpOn = true
                jumpAction = 0
            } else
                jumpAction = 1

        } else if (playerMovement.onGround && jumpAction > 0 && jumpAction < jumpActionTolerance) {
            playerMovement.onGround = false
            playerMovement.velocity.v1 = -jumpImpulse
            doubleJumpOn = false
            jumpAction = 0
        } else if (jumpAction > 0)
            jumpAction++
    }

    companion object :  ComponentSubTypeSystem<Control, PlatformerJumpController>(Control, "PlatformerJumpController") {
        override fun create() = PlatformerJumpController()
    }
}