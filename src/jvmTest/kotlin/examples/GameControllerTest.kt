package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Engine.Companion.SYSTEM_FONT
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.text.EText
import com.inari.firefly.graphics.view.ETransform
import org.lwjgl.glfw.GLFW
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun main() {
    DesktopApp("GameControllerTest", 800, 600) {
        var text1 = StringBuilder("")
        var text2 = StringBuilder("")
        var text3 = StringBuilder("")
        val updateCall: () -> Unit = {
            val bytes: ByteBuffer? = GLFW.glfwGetJoystickButtons(GLFW.GLFW_JOYSTICK_1)
            val axes: FloatBuffer? = GLFW.glfwGetJoystickAxes(GLFW.GLFW_JOYSTICK_1)
            val hats: ByteBuffer? = GLFW.glfwGetJoystickHats(GLFW.GLFW_JOYSTICK_1)
            if (bytes != null && axes != null && hats != null) {
                val bytesArray = ByteArray(bytes.remaining())
                val floatArray = FloatArray(axes.remaining())
                val hatsArray = ByteArray(hats.remaining())
                bytes.get(bytesArray, 0, bytesArray.size)
                axes.get(floatArray, 0, floatArray.size)
                hats.get(hatsArray, 0, hatsArray.size)
                text1.clear().append(bytesArray.contentToString())
                text2.clear().append(floatArray.contentToString())
                text3.clear().append(hats.get(0))
            }
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) {
                position(100, 100)
            }
            withComponent(EText) {
                fontRef(SYSTEM_FONT)
                text.append(text1)
                text1 = text
            }
        }
        Entity {
            autoActivation = true
            withComponent(ETransform) {
                position(100, 150)
            }
            withComponent(EText) {
                fontRef(SYSTEM_FONT)
                text.append(text2)
                text2 = text
            }
        }
        Entity {
            autoActivation = true
            withComponent(ETransform) {
                position(100, 200)
            }
            withComponent(EText) {
                fontRef(SYSTEM_FONT)
                text.append(text3)
                text3 = text
            }
        }
        Engine.registerListener(UPDATE_EVENT_TYPE, updateCall)
    }
}