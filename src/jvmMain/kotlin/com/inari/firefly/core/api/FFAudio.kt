package com.inari.firefly.core.api

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynIntArray


actual object FFAudio {

    private val sounds: DynArray<Sound> = DynArray.of()
    private val lastPlayingSoundOnChannel = DynIntArray(5, -1)
    private val music: DynArray<Music> = DynArray.of()

    actual fun createSound(resourceName: String, streaming: Boolean): Int =
        if (streaming)
            music.add(Gdx.audio.newMusic(Gdx.files.classpath(resourceName)))
        else
            sounds.add(Gdx.audio.newSound(Gdx.files.classpath(resourceName)))

    actual fun disposeSound(soundId: Int, streaming: Boolean) {
        if (streaming)
            music.remove(soundId)?.dispose()
        else
            sounds.remove(soundId)?.dispose()
    }

    actual fun changeMusic(soundId: Int, volume: Float, pan: Float) {
        val music = music[soundId] ?: return
        if (music.isPlaying) return
        music.setPan(pan, volume)
    }

    actual fun changeSound(soundId: Int, instanceId: Long, volume: Float, pitch: Float, pan: Float) {
        val sound = sounds[soundId] ?: return
        sound.setPan( instanceId, pan, volume )
        sound.setPitch( instanceId, pitch )
        sound.setVolume( instanceId, volume )
    }

    actual fun playMusic(soundId: Int, looping: Boolean, volume: Float, pan: Float) {
        val music = music[soundId] ?: return
        if (music.isPlaying) return
        music.setPan(pan, volume)
        music.isLooping = looping
        music.play()
    }

    actual fun playSound(soundId: Int, channel: Int, looping: Boolean, volume: Float, pitch: Float, pan: Float): Long {
        val sound = sounds[soundId] ?: return -1
        if (channel >= 0 && channel < lastPlayingSoundOnChannel.length && lastPlayingSoundOnChannel[channel] >= 0)
            sounds[lastPlayingSoundOnChannel[channel]]?.stop()

        lastPlayingSoundOnChannel[channel] = soundId

        val play = sound.play(volume, pitch, pan)
        if (looping)
            sound.setLooping(play, true)

        return play
    }

    actual fun stopMusic(soundId: Int) {
        val music = music[soundId] ?: return
        if (music.isPlaying) music.stop()
    }

    actual fun stopSound(soundId: Int, instanceId: Long) {
        sounds[soundId]?.stop(instanceId)
    }
}