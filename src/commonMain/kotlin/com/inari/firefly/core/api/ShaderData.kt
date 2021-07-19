package com.inari.firefly.core.api

import com.inari.firefly.NO_NAME
import com.inari.firefly.NO_PROGRAM
import com.inari.firefly.core.component.CompId
import com.inari.util.geom.Position
import com.inari.util.geom.PositionF
import com.inari.util.graphics.IColor
import kotlin.jvm.JvmField

class ShaderData(
    @JvmField var name: String = NO_NAME,
    @JvmField var vertexShaderResourceName: String = NO_NAME,
    @JvmField var vertexShaderProgram: String = NO_PROGRAM,
    @JvmField var fragmentShaderResourceName: String = NO_NAME,
    @JvmField var fragmentShaderProgram: String = NO_PROGRAM,
    @JvmField var shaderInit: ShaderInit = {}
) {

    fun reset() {
        name = NO_NAME
        vertexShaderResourceName = NO_NAME
        vertexShaderProgram = NO_PROGRAM
        fragmentShaderResourceName = NO_NAME
        fragmentShaderProgram = NO_PROGRAM
    }
}

typealias ShaderInit = (ShaderInitAdapter) -> Unit
interface ShaderInitAdapter {

    fun setUniformFloat(name: String, value: Float)

    fun setTexture(name: String, textureName: String)
    fun setTexture(name: String, textureId: CompId)
    fun setViewTexture(name: String, viewName: String)
    fun setViewTexture(name: String, viewId: CompId)

    fun setUniformVec2(name: String, position: PositionF)
    fun setUniformVec2(name: String, position: Position)
    fun setUniformColorVec4(name: String, color: IColor)

}