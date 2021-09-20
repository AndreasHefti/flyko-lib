package com.inari.firefly.graphics

import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.api.ShaderData
import com.inari.firefly.core.api.ShaderInit
import com.inari.firefly.core.system.SystemComponentSubType
import kotlin.jvm.JvmField

class ShaderAsset private constructor() : Asset() {

    @JvmField internal var shaderId: Int = -1
    override fun instanceId(index: Int): Int = shaderId

    private val data = ShaderData()

    override var name
        get() = data.name
        set(value) {
            super.name = value
            data.name = value
        }

    var vertShaderResource: String
        get() = data.vertexShaderResourceName
        set(value) {data.vertexShaderResourceName = setIfNotInitialized(value, "VertShaderResource")}
    var vertShaderProgram: String
        get() = data.vertexShaderProgram
        set(value) {data.vertexShaderProgram = setIfNotInitialized(value, "VertShaderProgram")}
    var fragShaderResource: String
        get() = data.fragmentShaderResourceName
        set(value) {data.fragmentShaderResourceName = setIfNotInitialized(value, "FragShaderResource")}
    var fragShaderProgram: String
        get() = data.fragmentShaderProgram
        set(value) {data.fragmentShaderProgram = setIfNotInitialized(value, "FragShaderProgram")}
    var shaderInit: ShaderInit
        get() = data.shaderInit
        set(value) {data.shaderInit = setIfNotInitialized(value, "ShaderInit")}

    override fun load() {
        if (shaderId < 0)
            shaderId = FFContext.graphics.createShader(data)
    }

    override fun unload() {
       if (shaderId >= 0) {
           FFContext.graphics.disposeShader(shaderId)
           shaderId = -1
       }
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Asset, ShaderAsset>(Asset, ShaderAsset::class) {
        override fun createEmpty() = ShaderAsset()
    }
}