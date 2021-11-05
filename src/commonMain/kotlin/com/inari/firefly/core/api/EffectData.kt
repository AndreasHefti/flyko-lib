package com.inari.firefly.core.api

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.NO_NAME
import com.inari.firefly.NO_PROGRAM
import com.inari.firefly.VOID_CONSUMER
import com.inari.util.collection.DynArray
import com.inari.util.geom.Position
import com.inari.util.geom.PositionF
import com.inari.util.graphics.IColor
import kotlin.jvm.JvmField


class EffectData(
    @JvmField var shaderData: ShaderData,
    @JvmField var shaderInit: ShaderInit = VOID_CONSUMER,
) {

    @JvmField internal val textureBindings = DynArray.of<TextureBind>(1, 1)
    @JvmField internal val fboTextureBindings = DynArray.of<FBOTextureBind>(1, 1)

    fun reset() {
        shaderData.reset()
        textureBindings.clear()
        fboTextureBindings.clear()
        shaderInit = VOID_CONSUMER
    }
}

class ShaderData(
    @JvmField var vertexShaderResourceName: String = NO_NAME,
    @JvmField var vertexShaderProgram: String = NO_PROGRAM,
    @JvmField var fragmentShaderResourceName: String = NO_NAME,
    @JvmField var fragmentShaderProgram: String = NO_PROGRAM,
) {

    fun reset() {
        vertexShaderResourceName = NO_NAME
        vertexShaderProgram = NO_PROGRAM
        fragmentShaderResourceName = NO_NAME
        fragmentShaderProgram = NO_PROGRAM
    }
}

class TextureBind(
    @JvmField val bindingName: String,
    @JvmField val textureId: Int,
)

class FBOTextureBind(
    @JvmField val bindingName: String,
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val clearColor: IColor
)

typealias ShaderInit = (ShaderInitAdapter) -> Unit
interface ShaderInitAdapter {
    fun setUniformFloat(name: String, value: Float)
    fun setUniformVec2(name: String, position: PositionF)
    fun setUniformVec2(name: String, position: Position)
    fun setUniformColorVec4(name: String, color: IColor)
}