package com.inari.firefly.audio

import com.inari.firefly.FFContext
import com.inari.firefly.control.ControlledSystemComponent
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponentSingleType
import kotlin.jvm.JvmField

class Sound private constructor() : TriggeredSystemComponent(Sound::class.simpleName!!), ControlledSystemComponent {

    @JvmField internal var soundAssetId: Int = -1
    @JvmField internal var playId: Long = -1

    val soundAsset = ComponentRefResolver(SoundAsset) { index -> soundAssetId = index }
    var looping: Boolean = false
    var volume: Float = 1.0f
    var pitch: Float = 1.0f
    var pan: Float = 0.0f
    var channel: Int = 0

    fun <A : Trigger> playTrigger(cBuilder: Trigger.Subtype<A>, configure: (A.() -> Unit)): A =
        super.trigger(cBuilder, { FFContext.activate(this) }, configure)

    fun <A : Trigger> stopTrigger(cBuilder: Trigger.Subtype<A>, configure: (A.() -> Unit)): A =
        super.trigger(cBuilder, { FFContext.deactivate(this) }, configure)

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<Sound>(Sound::class) {
        override fun createEmpty() = Sound()
    }
}