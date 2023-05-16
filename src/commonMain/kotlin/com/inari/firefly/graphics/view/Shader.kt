package com.inari.firefly.graphics.view

import com.inari.firefly.core.Asset
import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.NULL_BINDING_INDEX
import com.inari.firefly.core.api.ShaderData
import com.inari.firefly.core.api.ShaderUpdate
import com.inari.util.NO_NAME
import kotlin.jvm.JvmField

class Shader private constructor(): Asset(Shader) {

    @JvmField val renderData = ShaderData()

    var vertexShaderResourceName: String
        get() = renderData.vertexShaderResourceName
        set(value) { renderData.vertexShaderResourceName = checkNotLoaded(value, "vertexShaderResourceName") }
    var vertexShaderProgram: String
        get() = renderData.vertexShaderProgram
        set(value) { renderData.vertexShaderProgram = value }
    var fragmentShaderResourceName: String
        get() = renderData.fragmentShaderResourceName
        set(value) { renderData.fragmentShaderResourceName = checkNotLoaded(value, "fragmentShaderResourceName") }
    var fragmentShaderProgram: String
        get() = renderData.fragmentShaderProgram
        set(value) { renderData.fragmentShaderProgram = value }
    var shaderUpdate: (ShaderUpdate) -> Unit
        get() = renderData.shaderUpdate
        set(value) { renderData.shaderUpdate = checkNotLoaded(value, "shaderInit") }

    override fun load() {
        super.load()
        if (assetIndex >= 0)
            return

        // load vertex shader program if resource is defined
        if (vertexShaderResourceName != NO_NAME)
            vertexShaderProgram = loadShaderProgram(vertexShaderResourceName)

        // load fragment shader program if resource is defined
        if (fragmentShaderResourceName != NO_NAME)
            fragmentShaderProgram = loadShaderProgram(fragmentShaderResourceName)

        assetIndex = Engine.graphics.createShader(this.renderData)
    }

    override fun dispose() {
        if (assetIndex < 0) return
        Engine.graphics.disposeShader(assetIndex)
        assetIndex = NULL_BINDING_INDEX
    }

    private fun loadShaderProgram(resource: String): String {
        val shaderProgram = Engine.resourceService.loadTextResource(resource)
            .lines()
            .map {
                if (it.startsWith( "#pragma flyko-lib: import") )
                    loadShaderProgram(it.substring(it.indexOf("=") + 1).trim())
                else it
            }.reduce{ acc, s ->
                acc + "\n" + s
            }
        return shaderProgram
    }

    companion object : ComponentSubTypeBuilder<Asset, Shader>(Asset,"Shader") {
        override fun create() = Shader()
    }
}