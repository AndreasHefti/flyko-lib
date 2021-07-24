package com.inari.firefly.audio

import com.inari.firefly.FFContext
import com.inari.firefly.control.ControlledSystemComponent
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggerSystem.trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.Call
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

class Sound private constructor() : SystemComponent(Sound::class.simpleName!!), TriggeredSystemComponent, ControlledSystemComponent {

    @JvmField internal var soundAssetId: Int = -1
    @JvmField internal var playId: Long = -1

    private val playCall: Call = { FFContext.activate(this) }
    private val stopCall: Call = { FFContext.deactivate(this) }

    val soundAsset = ComponentRefResolver(SoundAsset) { index -> soundAssetId = index }
    var looping: Boolean = false
    var volume: Float = 1.0f
    var pitch: Float = 1.0f
    var pan: Float = 0.0f
    var channel: Int = 0

    fun <A : Trigger> withPlayTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A {
        val result = super.withTrigger(cBuilder, configure)
        result.call = playCall
        return result
    }

    fun <A : Trigger> withStopTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A  {
        val result = super.withTrigger(cBuilder, configure)
        result.call = stopCall
        return result
    }

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<Sound>(Sound::class) {
        override fun createEmpty() = Sound()
    }
}