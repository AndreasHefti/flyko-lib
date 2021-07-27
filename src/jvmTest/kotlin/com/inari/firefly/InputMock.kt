package com.inari.firefly

import com.inari.firefly.core.api.*

object InputMock : InputAPI {

    override
    val xpos: Int
        get() = 0

    override
    val ypos: Int
        get() = 0
    override val dx: Int
        get() = 0
    override val dy: Int
        get() = 0

    override val implementations: List<InputImpl> = listOf()
    override val devices: MutableMap<String, InputDevice>
        get() = TODO("Not yet implemented")

    override fun <T : InputDevice> createDevice(name: String, implementation: InputImpl, window: Long): T {
        TODO("Not yet implemented")
    }

    override fun getDevice(name: String): InputDevice {
        TODO("Not yet implemented")
    }

    override fun <T : InputDevice> getDeviceOf(name: String): T {
        TODO("Not yet implemented")
    }

    override fun createOrAdapter(name: String, a: String, b: String): ORAdapter {
        TODO("Not yet implemented")
    }

    override fun clearDevice(name: String) {
        TODO("Not yet implemented")
    }

    override fun setKeyCallback(callback: KeyCallback) {
        TODO("Not yet implemented")
    }

    override fun setMouseButtonCallback(callback: MouseCallback) {
        TODO("Not yet implemented")
    }

    override fun setButtonCallback(device: String, callback: ButtonCallback) {
        TODO("Not yet implemented")
    }

    override fun resetInputCallbacks() {
        TODO("Not yet implemented")
    }


}
