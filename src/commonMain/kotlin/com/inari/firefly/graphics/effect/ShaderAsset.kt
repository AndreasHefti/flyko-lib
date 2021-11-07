package com.inari.firefly.graphics.effect

import com.inari.firefly.FFContext
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.api.*
import com.inari.firefly.core.system.SystemComponentSubType
import kotlin.jvm.JvmField

class ShaderAsset private constructor() : Asset() {

    @JvmField internal var shaderId: Int = -1
    @JvmField internal val shaderData = ShaderData()

    var vertShaderResource: String
        get() = shaderData.vertexShaderResourceName
        set(value) { shaderData.vertexShaderResourceName = iSetIfNotInitialized(value, "vertShaderResource") }
    var vertShaderProgram: String
        get() = shaderData.vertexShaderProgram
        set(value) { shaderData.vertexShaderProgram = iSetIfNotInitialized(value, "vertShaderProgram") }
    var fragShaderResource: String
        get() = shaderData.fragmentShaderResourceName
        set(value) { shaderData.fragmentShaderResourceName = iSetIfNotInitialized(value, "fragShaderResource") }
    var fragShaderProgram: String
        get() = shaderData.fragmentShaderProgram
        set(value) { shaderData.fragmentShaderProgram = iSetIfNotInitialized(value, "fragShaderProgram") }
    var shaderInit: ShaderInit
        get() = shaderInit
        set(value) { shaderData.shaderInit = iSetIfNotInitialized(value, "shaderInit") }

    override fun instanceId(index: Int): Int = shaderId

    override fun load() {
        if (shaderId >= 0)
            return

        // load vertex shader program if resource is defined
        if (shaderData.vertexShaderResourceName != NO_NAME)
            shaderData.vertexShaderProgram =
                FFContext.loadShaderProgram(shaderData.vertexShaderResourceName)

        // load fragment shader program if resource is defined
        if (shaderData.fragmentShaderResourceName != NO_NAME)
            shaderData.fragmentShaderProgram =
                FFContext.loadShaderProgram(shaderData.fragmentShaderResourceName)

        shaderId = FFContext.graphics.createShader(shaderData)
    }

    override fun unload() {
        if (shaderId >= 0) {
            FFContext.graphics.disposeShader(shaderId)
            shaderId = -1
        }
    }

    private fun <T> iSetIfNotInitialized(value: T, name: String): T {
        return if (instanceId >= 0) super.alreadyInit(name)
        else super.setIfNotInitialized(value, name)
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Asset, ShaderAsset>(Asset, ShaderAsset::class) {
        override fun createEmpty() = ShaderAsset()
    }
}