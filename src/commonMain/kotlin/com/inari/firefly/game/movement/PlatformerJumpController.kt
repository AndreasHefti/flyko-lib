package com.inari.firefly.game.movement

import com.inari.firefly.FFContext
import com.inari.firefly.control.Controller
import com.inari.firefly.control.SingleComponentController
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.physics.movement.EMovement
import kotlin.jvm.JvmField

class PlatformerJumpController : SingleComponentController() {

    @JvmField var inputDevice: InputDevice = FFContext.input.getDefaultDevice()
    @JvmField var jumpButton = ButtonType.FIRE_1
    @JvmField var jumpImpulse = 100f
    @JvmField var doubleJump = true

    private var doubleJumpOn = true
    private lateinit var playerMovement: EMovement

    override fun register(componentId: CompId) {
        super.register(componentId)
        playerMovement = FFContext[EMovement, componentId]
    }

    override fun unregister(componentId: CompId, disposeWhenEmpty: Boolean) {
        playerMovement = null!!
        super.unregister(componentId, disposeWhenEmpty)
    }

    override fun update(componentId: CompId) {
        if (inputDevice.buttonTyped(jumpButton)) {
            if (playerMovement.onGround) {
                playerMovement.onGround = false
                playerMovement.velocity.dy = -jumpImpulse
                doubleJumpOn = false
            } else if (doubleJump && !doubleJumpOn) {
                playerMovement.velocity.dy = -jumpImpulse
                doubleJumpOn = true
            }
        }
    }

    companion object : SystemComponentSubType<Controller, PlatformerJumpController>(Controller, PlatformerJumpController::class) {
        override fun createEmpty() = PlatformerJumpController()
    }
}