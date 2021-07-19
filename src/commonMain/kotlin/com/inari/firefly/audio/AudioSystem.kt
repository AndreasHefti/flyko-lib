package com.inari.firefly.audio

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object AudioSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Sound)

    @JvmField val sounds = ComponentSystem.createComponentMapping(
        Sound,
        activationMapping = true,
        nameMapping = true,
        listener = { sound, action -> when (action) {
            CREATED       -> created(sound)
            ACTIVATED     -> activated(sound)
            DEACTIVATED   -> deactivated(sound)
            DELETED       -> deleted(sound)
        } }
    )

    private fun created(sound: Sound) =
        FFContext[SoundAsset, sound.soundAssetId].activate()

    private fun activated(sound: Sound) {
        val soundAsset = FFContext[SoundAsset, sound.soundAssetId]
        if ( soundAsset.streaming )
            FFContext.audio
                .playMusic(
                    soundAsset.id,
                    sound.looping,
                    sound.volume,
                    sound.pan
                )
        else
            sound.playId = FFContext.audio
                .playSound(
                    soundAsset.id,
                    sound.channel,
                    sound.looping,
                    sound.volume,
                    sound.pitch,
                    sound.pan
                )
    }

    private fun deactivated(sound: Sound) {
        val soundAsset = FFContext[SoundAsset, sound.soundAssetId]
        if ( soundAsset.streaming )
            FFContext.audio.stopMusic(soundAsset.id)
        else
            FFContext.audio.stopSound(soundAsset.id, sound.playId)
    }

    private fun deleted(sound: Sound) =
        FFContext[SoundAsset, sound.soundAssetId].deactivate()

    override fun clearSystem() = sounds.clear()

}