package com.inari.firefly.game.player.movement

import com.inari.firefly.FFContext
import com.inari.firefly.control.Controller
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.physics.movement.EMovement
import kotlin.jvm.JvmField
import kotlin.math.max
import kotlin.math.min

class PlatformerHMoveController : Controller() {

    @JvmField var runVelocityStep = 10.0f
    @JvmField var stopVelocityStep = 20f

    @JvmField var inputDevice: InputDevice = FFContext.input.getDefaultDevice()
    @JvmField var buttonLeft = ButtonType.LEFT
    @JvmField var buttonRight = ButtonType.RIGHT

    private lateinit var playerMovement: EMovement

    override fun init(componentId: CompId) {
        playerMovement = FFContext[EMovement, componentId]
    }

    override fun update(componentId: CompId) {
        if (inputDevice.buttonPressed(buttonLeft)) {
            if (playerMovement.velocity.dx <= -playerMovement.maxVelocityWest)
                return
            playerMovement.velocity.dx = max( -playerMovement.maxVelocityWest, playerMovement.velocity.dx - runVelocityStep)
        } else if (inputDevice.buttonPressed(buttonRight)) {
            if (playerMovement.velocity.dx >= playerMovement.maxVelocityWest)
                return
            playerMovement.velocity.dx = min(playerMovement.maxVelocityWest, playerMovement.velocity.dx + runVelocityStep)
        } else if (playerMovement.velocity.dx != 0.0f) {
            if (playerMovement.velocity.dx > 0f)
                playerMovement.velocity.dx = max(0.0f, playerMovement.velocity.dx - stopVelocityStep)
            else
                playerMovement.velocity.dx = min(0.0f, playerMovement.velocity.dx + stopVelocityStep)
        }
    }

    companion object : SystemComponentSubType<Controller, PlatformerHMoveController>(Controller, PlatformerHMoveController::class) {
        override fun createEmpty() = PlatformerHMoveController()
    }

}