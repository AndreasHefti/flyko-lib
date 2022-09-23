import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.*
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.FFInput
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.text.EText
import com.inari.util.Call
import org.lwjgl.glfw.GLFW

class ControllerInputTest : DesktopApp() {

    override val title: String = this::class.simpleName!!

    private var text = StringBuilder("")
    private var textId = NO_COMP_ID
    private val updateCall: Call = {
        val textEntity = FFContext[EText, textId]
        val keyInput = FFContext.input.getDevice("AllInput")
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

    override fun init() {

        val keyInput1 = FFContext.input.createDevice<FFInput.GLFWDesktopKeyboardInput>(
                "KeyInput1",
                FFInput.GLFWDesktopKeyboardInput)
        val keyInput2 = FFContext.input.createDevice<FFInput.GLFWDesktopKeyboardInput>(
                "KeyInput2",
                FFInput.GLFWDesktopKeyboardInput)
        val keyInput3 = FFContext.input.createDevice<FFInput.GLFWControllerInput>(
                "ControllerInput",
                FFInput.GLFWControllerInput)

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

        FFContext.input.createOrAdapter("KeyInput", "KeyInput1", "KeyInput2")
        FFContext.input.createOrAdapter("AllInput", "KeyInput", "ControllerInput")

        textId = Entity.buildAndActivate {
            withComponent(ETransform) {
                position(100, 100)
            }
            withComponent(EText) {
                fontAsset(SYSTEM_FONT)
                text.append(text)
                this@ControllerInputTest.text = text
            }
        }

        FFContext.registerListener(FFApp.UpdateEvent, updateCall)
    }

}

fun main() {
    try {
        val config = Lwjgl3ApplicationConfiguration()
        config.setResizable(true)
        config.setWindowedMode(704, 480)
        Lwjgl3Application(ControllerInputTest(), config)
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}