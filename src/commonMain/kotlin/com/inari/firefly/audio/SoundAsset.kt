package com.inari.firefly.audio

import com.inari.firefly.FFContext
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.system.SystemComponentSubType
import kotlin.jvm.JvmField

class SoundAsset private constructor() : Asset() {

    @JvmField internal var id: Int = -1

    var resourceName = NO_NAME
        set(value) {field = setIfNotInitialized(value, "resourceName")}
    var streaming = false
        set(value) {field = setIfNotInitialized(value, "streaming")}

    override fun instanceId(index: Int): Int = id

    override fun load() {
        if (id < 0)
            id = FFContext.audio.createSound(resourceName, streaming)
    }

    override fun unload() {
        if (id >= 0)
            FFContext.audio.disposeSound(id, streaming)
        id = -1
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Asset, SoundAsset>(Asset, SoundAsset::class) {
        override fun createEmpty() = SoundAsset()
    }
}