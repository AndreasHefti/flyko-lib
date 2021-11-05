package com.inari.firefly.graphics.effect

import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.api.*
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.TextureAsset
import com.inari.util.graphics.IColor
import kotlin.jvm.JvmField

class ShaderEffectAsset private constructor() : Asset() {

    @JvmField internal var effectId: Int = -1
    @JvmField internal val effectData = EffectData(ShaderData())

    var vertShaderResource: String
        get() = effectData.shaderData.vertexShaderResourceName
        set(value) { effectData.shaderData.vertexShaderResourceName = iSetIfNotInitialized(value, "vertShaderResource") }
    var vertShaderProgram: String
        get() = effectData.shaderData.vertexShaderProgram
        set(value) { effectData.shaderData.vertexShaderProgram = iSetIfNotInitialized(value, "vertShaderProgram") }
    var fragShaderResource: String
        get() = effectData.shaderData.fragmentShaderResourceName
        set(value) { effectData.shaderData.fragmentShaderResourceName = iSetIfNotInitialized(value, "fragShaderResource") }
    var fragShaderProgram: String
        get() = effectData.shaderData.fragmentShaderProgram
        set(value) { effectData.shaderData.fragmentShaderProgram = iSetIfNotInitialized(value, "fragShaderProgram") }
    var shaderInit: ShaderInit
        get() = effectData.shaderInit
        set(value) { effectData.shaderInit = iSetIfNotInitialized(value, "shaderInit") }

    fun withTextureBinding(bindingName: String): AssetInstanceRefResolver {
        return AssetInstanceRefResolver(
            { index -> effectData.textureBindings.add(TextureBind(bindingName, index)) },
            { -1 })
    }

    fun withFBOTextureBinding(bindingName: String, texWidth: Int, texHeight: Int, clearColor: IColor = IColor.BLACK) =
        effectData.fboTextureBindings.add(FBOTextureBind(bindingName, texWidth, texHeight, clearColor))

    override fun instanceId(index: Int): Int = effectId

    override fun load() {
        if (effectId >= 0)
            return

        effectData.textureBindings.forEach { FFContext.activate(TextureAsset, it.textureId) }
        effectId = FFContext.graphics.createEffect(effectData)
    }

    override fun unload() {
        if (effectId >= 0) {
            FFContext.graphics.disposeEffect(effectId)
            effectId = -1
        }
    }

    private fun <T> iSetIfNotInitialized(value: T, name: String): T {
        return if (instanceId >= 0) super.alreadyInit(name)
        else super.setIfNotInitialized(value, name)
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Asset, ShaderEffectAsset>(Asset, ShaderEffectAsset::class) {
        override fun createEmpty() = ShaderEffectAsset()
    }
}