package com.inari.firefly.physics.sound

import com.inari.firefly.core.*
import com.inari.util.ZERO_INT
import kotlin.jvm.JvmField

class Sound private constructor(): Play(Sound), Controlled, TriggeredComponent {

    var pitch: Float = 1.0f
        set(value) {
            field = value
            change()
        }
    @JvmField var channel: Int = ZERO_INT

    override val streaming: Boolean = true

    override fun change() {
        if (playId >= 0)
            Engine.audio.changeSound(assetIndex, playId, volume, pitch, pan)
    }

    override fun play() {
        playId = Engine.audio.playSound(assetIndex, channel, looping, volume, pitch, pan)
    }

    override fun stop() {
        Engine.audio.stopSound(assetIndex, playId)
        playId = -1
    }

    companion object : ComponentSubTypeBuilder<Asset, Sound>(Asset,"Sound") {
        override fun create() = Sound()
    }
}