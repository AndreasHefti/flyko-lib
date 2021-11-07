package com.inari.firefly.graphics.effect

import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.graphics.view.View
import kotlin.jvm.JvmField

class Effect private constructor() : SystemComponent(Effect::class.simpleName!!) {

    @JvmField internal var shaderRef = -1
    @JvmField internal var sourceTextureRef = -1
    @JvmField internal var sourceBackBufferRef = -1
    @JvmField internal var sourceViewRef = -1
    @JvmField internal var targetBackBufferRef = -1

    val shader = AssetInstanceRefResolver(
        { index -> shaderRef = index },
        { shaderRef })

    val sourceTexture = AssetInstanceRefResolver(
        { instanceId -> sourceTextureRef = instanceId },
        { sourceTextureRef })
    val sourceBackBuffer = ComponentRefResolver(BackBuffer) { index-> sourceBackBufferRef = index }
    val sourceView = ComponentRefResolver(View) { index-> sourceViewRef = index }
    val targetBackBuffer = ComponentRefResolver(BackBuffer) { index-> targetBackBufferRef = index }


    override fun componentType() = Companion
    companion object : SystemComponentSingleType<Effect>(Effect::class) {
        override fun createEmpty() = Effect()
    }
}