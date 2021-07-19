package com.inari.firefly.core.api

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.core.api.InputDevice.Companion.ACTION_PRESS
import com.inari.firefly.core.api.InputDevice.Companion.ACTION_TYPED
import com.inari.firefly.core.api.InputDevice.Companion.VOID_INPUT_DEVICE
import com.inari.util.Call
import kotlin.collections.HashMap


actual object FFInput {

    actual val xpos: Int
        get() = throw UnsupportedOperationException()
    actual val ypos: Int
        get() = throw UnsupportedOperationException()
    actual val dx: Int
        get() = throw UnsupportedOperationException()
    actual val dy: Int
        get() = throw UnsupportedOperationException()

    actual val implementations: List<InputImpl> = throw UnsupportedOperationException()

    actual val devices: MutableMap<String, InputDevice> = HashMap()

    init {
        devices[VOID_INPUT_DEVICE] = VOIDAdapter()
    }

    actual fun <T : InputDevice> createDevice(
            name: String,
            implementation: InputImpl,
            window: Long): T {

        throw UnsupportedOperationException()
    }

    actual fun createOrAdapter(name: String, a: String, b: String): ORAdapter {
        val adapter = ORAdapter(getDevice(a), getDevice(b), name)
        devices[name] = adapter
        return adapter
    }

    actual fun getDevice(name: String): InputDevice =
            devices[name] ?: devices[VOID_INPUT_DEVICE]!!

    actual fun <T : InputDevice> getDeviceOf(name: String): T = getDevice(name) as T

    actual fun clearDevice(name: String) {
        devices.remove(name)
    }

    actual fun setKeyCallback(callback: KeyCallback) {
        throw UnsupportedOperationException()
    }

    actual fun setMouseButtonCallback(callback: MouseCallback) {
        throw UnsupportedOperationException()
    }

    private var buttonCallbackUpdate: Call = {}
    actual fun setButtonCallback(deviceName: String, callback: ButtonCallback) {
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
            FFContext.registerListener(FFApp.UpdateEvent, buttonCallbackUpdate)
        } else throw IllegalArgumentException("No device with name: $deviceName found")
    }

    actual fun resetInputCallbacks() {
        throw UnsupportedOperationException()
    }


}