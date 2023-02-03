package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ShaderData
import com.inari.firefly.core.api.ShaderUpdate
import com.inari.util.NO_NAME
import com.inari.util.NO_PROGRAM
import com.inari.util.VOID_CALL
import com.inari.util.VOID_CONSUMER

class Shader private constructor(): Asset(Shader), ShaderData {

    override var vertexShaderResourceName: String = NO_NAME
        set(value) { field = checkNotLoaded(value, "vertexShaderResourceName") }
    override var vertexShaderProgram: String = NO_PROGRAM
    override var fragmentShaderResourceName: String = NO_NAME
        set(value) { field = checkNotLoaded(value, "fragmentShaderResourceName") }
    override var fragmentShaderProgram: String = NO_PROGRAM
    override var shaderUpdate: (ShaderUpdate) -> Unit = VOID_CONSUMER
        set(value) { field = checkNotLoaded(value, "shaderInit") }

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

        assetIndex = Engine.graphics.createShader(this)
    }

    override fun dispose() {
        if (assetIndex < 0) return
        Engine.graphics.disposeShader(assetIndex)
        assetIndex = -1
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