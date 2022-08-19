package com.inari.firefly.core.api

actual object AudioAPIImpl : AudioAPI {

    actual override fun createSound(resourceName: String, streaming: Boolean): Int =
        throw UnsupportedOperationException()

    actual override fun disposeSound(soundId: Int, streaming: Boolean) {
        throw UnsupportedOperationException()
    }

    actual override fun changeMusic(soundId: Int, volume: Float, pan: Float) {
        throw UnsupportedOperationException()
    }

    actual override fun changeSound(soundId: Int, instanceId: Long, volume: Float, pitch: Float, pan: Float) {
        throw UnsupportedOperationException()
    }

    actual override fun playMusic(soundId: Int, looping: Boolean, volume: Float, pan: Float) {
        throw UnsupportedOperationException()
    }

    actual override fun playSound(soundId: Int, channel: Int, looping: Boolean, volume: Float, pitch: Float, pan: Float): Long {
        throw UnsupportedOperationException()
    }

    actual override fun stopMusic(soundId: Int) {
        throw UnsupportedOperationException()
    }

    actual override fun stopSound(soundId: Int, instanceId: Long) {
        throw UnsupportedOperationException()
    }
}