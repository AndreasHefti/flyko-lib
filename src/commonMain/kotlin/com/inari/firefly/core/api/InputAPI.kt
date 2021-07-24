package com.inari.firefly.core.api

import com.inari.util.collection.BitSet

typealias KeyCallback = (Int, Int, Int) -> Unit // key, scancode, action
typealias MouseCallback = (Int, Int) -> Unit    // button, action
typealias JoystickConnectionCallback = (Int, Int) -> Unit // joystickId, action
typealias ButtonCallback = (ButtonType, Int) -> Unit

enum class DeviceType {
    KEYBOARD,
    MOUSE,
    JOYSTICK_PAD,
    TOUCH,
    OR,
    VOID
}

enum class ButtonType {
    UP,
    RIGHT,
    DOWN,
    LEFT,

    ENTER,
    QUIT,
    FIRE_1,
    FIRE_2,

    BUTTON_A,
    BUTTON_B,
    BUTTON_C,
    BUTTON_D,

    BUTTON_W,
    BUTTON_X,
    BUTTON_Y,
    BUTTON_Z,

    BUTTON_0,
    BUTTON_1,
    BUTTON_2,
    BUTTON_3,
    BUTTON_4,
    BUTTON_5,
    BUTTON_6,
    BUTTON_7,
    BUTTON_8,
    BUTTON_9
}

interface InputAPI {

    val xpos: Int
    val ypos: Int
    val dx: Int
    val dy: Int

    val implementations: List<InputImpl>
    val devices: MutableMap<String, InputDevice>

    fun <T : InputDevice> createDevice(name: String, implementation: InputImpl, window: Long = -1): T
    fun getDevice(name: String): InputDevice
    fun <T : InputDevice> getDeviceOf(name: String): T
    fun createOrAdapter(name: String, a: String, b: String): ORAdapter
    fun clearDevice(name: String)

    fun setKeyCallback(callback: KeyCallback)
    fun setMouseButtonCallback(callback: MouseCallback)
    fun setButtonCallback(deviceName: String, callback: ButtonCallback)
    fun resetInputCallbacks()

}

interface InputImpl {
    val type: DeviceType
    fun <T : InputDevice> create(window: Long = 0): T


}

interface InputDevice {

    val name: String
    val type: InputImpl
    val window: Long

    fun buttonTyped(button: ButtonType): Boolean
    fun buttonPressed(button: ButtonType): Boolean

    companion object {
        const val ACTION_PRESS = 1
        const val ACTION_TYPED = 2
        const val ACTION_CONNECTED = 0x40001
        const val ACTION_DISCONNECTED = 0x40002
        const val VOID_INPUT_DEVICE = "VOID_INPUT_DEVICE"
    }
}

interface KeyInput : InputDevice {
    fun mapKeyInput(buttonType: ButtonType, keyCode: Int)
}

interface MouseInput : InputDevice {
    val xPosition: Float
    val yPosition: Float
    val xScroll: Float
    val yScroll: Float
    fun mapButtonInput(buttonType: ButtonType, mouseButton: Int)
}

interface ControllerInput : InputDevice {
    var slot: Int
    fun axis(axisType: Int): Float
    fun mapButtonInput(buttonType: ButtonType, padButton: Int)
    fun mapHatInput(buttonType: ButtonType, padHat: Int)
    fun mapAxisButtonInput(buttonType: ButtonType, axisButton: Int)

}

class ORAdapter(
    val a: InputDevice,
    val b: InputDevice,
    override val name: String = "OR",
    override val window: Long = -1
) : InputDevice {
    override val type: InputImpl = Companion
    private val pressedCodeMapping = BitSet()
    override fun buttonPressed(button: ButtonType): Boolean =
        a.buttonPressed(button) || b.buttonPressed(button)
    override fun buttonTyped(button: ButtonType): Boolean {
        val buttonCode = button.ordinal
        val pressed = a.buttonTyped(button) || b.buttonTyped(button)
        if (pressed && pressedCodeMapping[buttonCode])
            return false

        pressedCodeMapping.set(buttonCode, pressed)
        return pressed
    }
    companion object : InputImpl {
        override val type = DeviceType.OR
        override fun <T : InputDevice> create(window: Long): T =
            throw UnsupportedOperationException()
    }
}

class VOIDAdapter(
    override val name: String = InputDevice.VOID_INPUT_DEVICE,
    override val window: Long = -1
) : InputDevice {
    override val type: InputImpl = Companion
    override fun buttonPressed(button: ButtonType): Boolean = false
    override fun buttonTyped(button: ButtonType): Boolean = false
    companion object : InputImpl {
        override val type = DeviceType.VOID
        override fun <T : InputDevice> create(window: Long): T =
            throw UnsupportedOperationException()
    }
}

data class ControllerDef(
    val id: Int,
    val name: String
)