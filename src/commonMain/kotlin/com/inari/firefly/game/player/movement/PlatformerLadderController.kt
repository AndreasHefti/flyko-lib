package com.inari.firefly.game.player.movement

import com.inari.firefly.FFContext
import com.inari.firefly.control.Controller
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.game.player.PlayerSystem.PLAYER_ASPECT_GROUP
import com.inari.firefly.physics.movement.EMovement
import kotlin.jvm.JvmField

class PlatformerLadderController private constructor(): Controller() {

    @JvmField var inputDevice: InputDevice = FFContext.input.getDefaultDevice()
    @JvmField var buttonUp = ButtonType.UP
    @JvmField var buttonDown = ButtonType.DOWN

    private lateinit var playerMovement: EMovement

    override fun init(componentId: CompId) {
        playerMovement = FFContext[EMovement, componentId]
    }

    override fun update(componentId: CompId) {
        TODO("Not yet implemented")
    }

    companion object : SystemComponentSubType<Controller, PlatformerLadderController>(Controller, PlatformerLadderController::class) {
        override fun createEmpty() = PlatformerLadderController()

        @JvmField val LADDER_PLAYER_ASPECT = PLAYER_ASPECT_GROUP.createAspect("OnLadder")
    }


}