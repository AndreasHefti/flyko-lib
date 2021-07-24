package com.inari.firefly.core.api


expect object FFInput {

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
