package com.inari.firefly.physics.sound

import com.inari.firefly.core.*
import com.inari.util.NO_NAME
import com.inari.util.ZERO_FLOAT
import kotlin.jvm.JvmField

abstract class Play protected constructor(subType: ComponentType<out Play>) : Asset(subType), TriggeredComponent {

    @JvmField internal var playId: Long = -1

    var resourceName = NO_NAME
        set(value) { field = checkNotLoaded(value, "ResourceName") }

    @JvmField var looping: Boolean = false
    var volume: Float = 1.0f
        set(value) {
            field = value
            change()
        }
    var pan: Float = ZERO_FLOAT
        set(value) {
            field = value
            change()
        }

    override fun load() {
        if (assetIndex >= 0) return
        assetIndex = Engine.audio.createSound(resourceName, streaming)
    }

    override fun dispose() {
        if (assetIndex < 0) return
        Engine.audio.disposeSound(assetIndex, streaming)
        assetIndex = -1
    }

    abstract fun play()
    abstract fun stop()
    protected abstract fun change()
    protected abstract val streaming: Boolean

    override fun activate() = play()
    override fun deactivate() = stop()
}

class Music private constructor(): Play(Music) {

    override val streaming: Boolean = true

    override fun change() {
        if (assetIndex >= 0)
            Engine.audio.changeMusic(assetIndex, volume, pan)
    }

    override fun play() {
        if (assetIndex >= 0)
            Engine.audio.playMusic(assetIndex, looping, volume, pan)
    }

    override fun stop() {
        if (assetIndex >= 0)
            Engine.audio.stopMusic(assetIndex)
    }

    companion object : ComponentSubTypeBuilder<Asset, Music>(Asset,"Music") {
        override fun create() = Music()
    }
}