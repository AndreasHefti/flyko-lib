package com.inari.firefly.game.player.movement

import com.inari.firefly.FFContext
import com.inari.firefly.control.Controller
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.physics.movement.EMovement
import kotlin.jvm.JvmField

class PlatformerJumpController : Controller() {

    @JvmField var inputDevice: InputDevice = FFContext.input.getDefaultDevice()
    @JvmField var jumpButton = ButtonType.FIRE_1
    @JvmField var jumpImpulse = 100f
    @JvmField var doubleJump = false
    @JvmField var jumpActionTolerance = 5

    private var jumpAction = 0
    private var doubleJumpOn = true
    private lateinit var playerMovement: EMovement

    override fun init(componentId: CompId) {
        playerMovement = FFContext[EMovement, componentId]
    }

    override fun update(componentId: CompId) {
        if (inputDevice.buttonTyped(jumpButton)) {
            if (playerMovement.onGround) {
                playerMovement.onGround = false
                playerMovement.velocity.dy = -jumpImpulse
                doubleJumpOn = false
                jumpAction = 0
            } else if (doubleJump && !doubleJumpOn) {
                playerMovement.velocity.dy = -jumpImpulse
                doubleJumpOn = true
                jumpAction = 0
            } else
                jumpAction = 1

        } else if (playerMovement.onGround && jumpAction > 0 && jumpAction < jumpActionTolerance) {
            playerMovement.onGround = false
            playerMovement.velocity.dy = -jumpImpulse
            doubleJumpOn = false
            jumpAction = 0
        } else if (jumpAction > 0)
            jumpAction++
    }

    companion object : SystemComponentSubType<Controller, PlatformerJumpController>(Controller, PlatformerJumpController::class) {
        override fun createEmpty() = PlatformerJumpController()
    }
}