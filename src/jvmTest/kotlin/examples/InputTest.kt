package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Component
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Engine.Companion.SYSTEM_FONT
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputAPIImpl
import com.inari.firefly.graphics.text.EText
import com.inari.firefly.graphics.view.ETransform
import org.lwjgl.glfw.GLFW


fun main(args: Array<String>) {

    var text = StringBuilder("")
    var textId = Component.NO_COMPONENT_KEY
    val updateCall: () -> Unit = {
        val textEntity = Entity[textId][EText]
        val keyInput = Engine.input.getDevice("AllInput")
        when {
            keyInput.buttonPressed(ButtonType.UP) -> textEntity.text.clear().append("UP")
            keyInput.buttonPressed(ButtonType.RIGHT) -> textEntity.text.clear().append("RIGHT")
            keyInput.buttonPressed(ButtonType.DOWN) -> textEntity.text.clear().append("DOWN")
            keyInput.buttonPressed(ButtonType.LEFT) -> textEntity.text.clear().append("LEFT")
            keyInput.buttonPressed(ButtonType.FIRE_1) -> textEntity.text.clear().append("FIRE_1")
            keyInput.buttonPressed(ButtonType.FIRE_2) -> textEntity.text.clear().append("FIRE_2")
            keyInput.buttonPressed(ButtonType.BUTTON_A) -> textEntity.text.clear().append("BUTTON_A")
            keyInput.buttonPressed(ButtonType.BUTTON_B) -> textEntity.text.clear().append("BUTTON_B")
            keyInput.buttonPressed(ButtonType.BUTTON_X) -> textEntity.text.clear().append("BUTTON_X")
            keyInput.buttonPressed(ButtonType.BUTTON_Y) -> textEntity.text.clear().append("BUTTON_Y")
            keyInput.buttonPressed(ButtonType.FIRE_2) -> textEntity.text.clear().append("FIRE_2")
            keyInput.buttonPressed(ButtonType.QUIT) -> textEntity.text.clear().append("QUIT")
            else -> textEntity.text.clear().append("--")
        }
    }

    DesktopApp("InputTest", 400, 400) {
        val keyInput1 = Engine.input.createDevice<InputAPIImpl.GLFWDesktopKeyboardInput>(
            "KeyInput1",
            InputAPIImpl.GLFWDesktopKeyboardInput)
        val keyInput2 = Engine.input.createDevice<InputAPIImpl.GLFWDesktopKeyboardInput>(
            "KeyInput2",
            InputAPIImpl.GLFWDesktopKeyboardInput)
        val keyInput3 = Engine.input.createDevice<InputAPIImpl.GLFWControllerInput>(
            "ControllerInput",
            InputAPIImpl.GLFWControllerInput)

        keyInput1.mapKeyInput(ButtonType.UP, GLFW.GLFW_KEY_W)
        keyInput1.mapKeyInput(ButtonType.RIGHT, GLFW.GLFW_KEY_D)
        keyInput1.mapKeyInput(ButtonType.DOWN, GLFW.GLFW_KEY_S)
        keyInput1.mapKeyInput(ButtonType.LEFT, GLFW.GLFW_KEY_A)
        keyInput1.mapKeyInput(ButtonType.FIRE_1, GLFW.GLFW_KEY_SPACE)
        keyInput1.mapKeyInput(ButtonType.FIRE_2, GLFW.GLFW_KEY_RIGHT_ALT)
        keyInput1.mapKeyInput(ButtonType.QUIT, GLFW.GLFW_KEY_ESCAPE)

        keyInput2.mapKeyInput(ButtonType.UP, GLFW.GLFW_KEY_UP)
        keyInput2.mapKeyInput(ButtonType.RIGHT, GLFW.GLFW_KEY_RIGHT)
        keyInput2.mapKeyInput(ButtonType.DOWN, GLFW.GLFW_KEY_DOWN)
        keyInput2.mapKeyInput(ButtonType.LEFT, GLFW.GLFW_KEY_LEFT)

        keyInput3.slot = 0
        keyInput3.mapHatInput(ButtonType.UP, GLFW.GLFW_HAT_UP)
        keyInput3.mapHatInput(ButtonType.RIGHT, GLFW.GLFW_HAT_RIGHT)
        keyInput3.mapHatInput(ButtonType.DOWN, GLFW.GLFW_HAT_DOWN)
        keyInput3.mapHatInput(ButtonType.LEFT, GLFW.GLFW_HAT_LEFT)

        keyInput3.mapAxisButtonInput(ButtonType.UP, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP)
        keyInput3.mapAxisButtonInput(ButtonType.RIGHT, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT)
        keyInput3.mapAxisButtonInput(ButtonType.DOWN, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN)
        keyInput3.mapAxisButtonInput(ButtonType.LEFT, GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT)

        keyInput3.mapButtonInput(ButtonType.BUTTON_A, GLFW.GLFW_GAMEPAD_BUTTON_A)
        keyInput3.mapButtonInput(ButtonType.BUTTON_B, GLFW.GLFW_GAMEPAD_BUTTON_B)
        keyInput3.mapButtonInput(ButtonType.BUTTON_X, GLFW.GLFW_GAMEPAD_BUTTON_X)
        keyInput3.mapButtonInput(ButtonType.BUTTON_Y, GLFW.GLFW_GAMEPAD_BUTTON_Y)
        keyInput3.mapButtonInput(ButtonType.QUIT, GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER)
        keyInput3.mapButtonInput(ButtonType.ENTER, GLFW.GLFW_GAMEPAD_BUTTON_START)

        Engine.input.createOrAdapter("KeyInput", "KeyInput1", "KeyInput2")
        Engine.input.createOrAdapter("AllInput", "KeyInput", "ControllerInput")

        textId = Entity {
            autoActivation = true
            withComponent(ETransform) {
                position(100, 100)
            }
            withComponent(EText) {
                fontRef(SYSTEM_FONT)
                text.append(text)
                text = this.text
            }
        }

        Engine.registerListener(UPDATE_EVENT_TYPE, updateCall)
    }
}