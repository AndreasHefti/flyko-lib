package com.inari.firefly.core.api

import com.inari.firefly.NO_NAME
import com.inari.firefly.NO_PROGRAM
import com.inari.firefly.VOID_CONSUMER
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector2i
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class ShaderData(
    @JvmField var vertexShaderResourceName: String = NO_NAME,
    @JvmField var vertexShaderProgram: String = NO_PROGRAM,
    @JvmField var fragmentShaderResourceName: String = NO_NAME,
    @JvmField var fragmentShaderProgram: String = NO_PROGRAM,
    @JvmField var shaderInit: ShaderInit = VOID_CONSUMER,
) {

    fun reset() {
        vertexShaderResourceName = NO_NAME
        vertexShaderProgram = NO_PROGRAM
        fragmentShaderResourceName = NO_NAME
        fragmentShaderProgram = NO_PROGRAM
        shaderInit = VOID_CONSUMER
    }
}

typealias ShaderInit = (ShaderInitAdapter) -> Unit
interface ShaderInitAdapter {
    fun setUniformFloat(bindingName: String, value: Float)
    fun setUniformVec2(bindingName: String, position: Vector2f)
    fun setUniformVec2(bindingName: String, position: Vector2i)
    fun setUniformColorVec4(bindingName: String, color: Vector4f)
    fun bindTexture(bindingName: String, textureId: Int)
    fun bindBackBuffer(bindingName: String, backBufferId: Int)
}