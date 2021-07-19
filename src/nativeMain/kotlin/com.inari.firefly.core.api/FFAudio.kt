package com.inari.firefly.core.api

actual object FFAudio {

    actual fun createSound(resourceName: String, streaming: Boolean): Int =
        throw UnsupportedOperationException()

    actual fun disposeSound(soundId: Int, streaming: Boolean) {
        throw UnsupportedOperationException()
    }

    actual fun changeMusic(soundId: Int, volume: Float, pan: Float) {
        throw UnsupportedOperationException()
    }

    actual fun changeSound(soundId: Int, instanceId: Long, volume: Float, pitch: Float, pan: Float) {
        throw UnsupportedOperationException()
    }

    actual fun playMusic(soundId: Int, looping: Boolean, volume: Float, pan: Float) {
        throw UnsupportedOperationException()
    }

    actual fun playSound(soundId: Int, channel: Int, looping: Boolean, volume: Float, pitch: Float, pan: Float): Long {
        throw UnsupportedOperationException()
    }

    actual fun stopMusic(soundId: Int) {
        throw UnsupportedOperationException()
    }

    actual fun stopSound(soundId: Int, instanceId: Long) {
        throw UnsupportedOperationException()
    }
}