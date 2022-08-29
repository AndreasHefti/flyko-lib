package com.inari.firefly.core.api

import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.InputDevice.Companion.ACTION_PRESS
import com.inari.firefly.core.api.InputDevice.Companion.ACTION_TYPED
import com.inari.firefly.core.api.InputDevice.Companion.VOID_INPUT_DEVICE
import com.inari.util.NO_NAME
import kotlin.collections.HashMap


actual object InputAPIImpl : InputAPI{

    actual override val xpos: Int
        get() = throw UnsupportedOperationException()
    actual override val ypos: Int
        get() = throw UnsupportedOperationException()
    actual override val dx: Int
        get() = throw UnsupportedOperationException()
    actual override val dy: Int
        get() = throw UnsupportedOperationException()

    actual override val implementations: List<InputImpl> = throw UnsupportedOperationException()

    actual override val devices: MutableMap<String, InputDevice> = HashMap()
    override var defaultDevice: String = NO_NAME

    init {
        devices[VOID_INPUT_DEVICE] = VOIDAdapter()
    }

    actual override fun <T : InputDevice> createDevice(
            name: String,
            implementation: InputImpl,
            window: Long): T {

        throw UnsupportedOperationException()
    }

    actual override fun createOrAdapter(name: String, a: String, b: String): ORAdapter {
        val adapter = ORAdapter(getDevice(a), getDevice(b), name)
        devices[name] = adapter
        return adapter
    }

    actual override fun getDevice(name: String): InputDevice =
            devices[name] ?: devices[VOID_INPUT_DEVICE]!!

    @Suppress("UNCHECKED_CAST")
    actual override fun <T : InputDevice> getDeviceOf(name: String): T = getDevice(name) as T

    actual override fun clearDevice(name: String) {
        devices.remove(name)
    }

    actual override fun setKeyCallback(callback: KeyCallback) {
        throw UnsupportedOperationException()
    }

    actual override fun setMouseButtonCallback(callback: MouseCallback) {
        throw UnsupportedOperationException()
    }

    private var buttonCallbackUpdate: () -> Unit = {}
    actual override fun setButtonCallback(deviceName: String, callback: ButtonCallback) {
        if (deviceName in devices) {
            val device = devices[deviceName]!!
            val buttonTypes = ButtonType.values()
            buttonCallbackUpdate = {
                buttonTypes.forEach {
                    if (device.buttonPressed(it))
                        callback.invoke(it, ACTION_TYPED)
                    if (device.buttonPressed(it))
                        callback.invoke(it, ACTION_PRESS)
                }
            }
            Engine.registerListener(Engine.UPDATE_EVENT_TYPE, buttonCallbackUpdate)
        } else throw IllegalArgumentException("No device with name: $deviceName found")
    }

    actual override fun resetInputCallbacks() {
        throw UnsupportedOperationException()
    }


}