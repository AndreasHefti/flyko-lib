package com.inari.firefly.game.player.movement

import com.inari.firefly.FFContext
import com.inari.firefly.ZERO_FLOAT
import com.inari.firefly.control.Controller
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.game.VOID_MOVE_CALLBACK
import com.inari.firefly.physics.movement.EMovement
import kotlin.jvm.JvmField
import kotlin.math.max
import kotlin.math.min

class PlatformerHMoveController : Controller() {

    @JvmField var runVelocityStep = 10.0f
    @JvmField var stopVelocityStep = 20.0f
    @JvmField var moveOnAir = true

    @JvmField var inputDevice: InputDevice = FFContext.input.getDefaultDevice()
    @JvmField var buttonLeft = ButtonType.LEFT
    @JvmField var buttonRight = ButtonType.RIGHT

    @JvmField var directionChangeCallback = VOID_MOVE_CALLBACK

    private lateinit var playerMovement: EMovement

    override fun init(componentId: CompId) {
        playerMovement = FFContext[EMovement, componentId]
    }

    override fun update(componentId: CompId) {
        if (!moveOnAir &&!playerMovement.onGround)
            return

        if (inputDevice.buttonPressed(buttonLeft)) {
            with (playerMovement) {
                val outRef = this@PlatformerHMoveController
                if (velocity.v0 <= -maxVelocityWest)
                    return

                if (velocity.v0 > ZERO_FLOAT) {
                    velocity.v0 = max(ZERO_FLOAT, velocity.v0 - outRef.stopVelocityStep)
                    outRef.directionChangeCallback(componentId.index, velocity.v0, outRef.buttonLeft)
                }
                else
                    velocity.v0 = max( -maxVelocityWest, velocity.v0 - outRef.runVelocityStep)
            }
        } else if (inputDevice.buttonPressed(buttonRight)) {
            with (playerMovement) {
                val outRef = this@PlatformerHMoveController
                if (velocity.v0 >= maxVelocityWest)
                    return
                if (velocity.v0 < ZERO_FLOAT) {
                    velocity.v0 = min(ZERO_FLOAT, velocity.v0 + outRef.stopVelocityStep)
                    outRef.directionChangeCallback(componentId.index, velocity.v0, outRef.buttonRight)
                }
                else
                    velocity.v0 = min(maxVelocityWest, velocity.v0 + outRef.runVelocityStep)
            }
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

    companion object : SystemComponentSubType<Controller, PlatformerHMoveController>(Controller, PlatformerHMoveController::class) {
        override fun createEmpty() = PlatformerHMoveController()
    }

}