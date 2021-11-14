package com.inari.firefly.graphics.text

import com.inari.firefly.BlendMode
import com.inari.firefly.core.api.TransformData
import com.inari.firefly.core.component.ComponentDSL
import com.inari.util.geom.PositionF
import com.inari.util.geom.Vector2f
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

@ComponentDSL
class CharacterMetaData private constructor(){

    @JvmField internal val transformData = TransformData()

    val position: PositionF
        get() = transformData.position
    val pivot: PositionF
        get() = transformData.pivot
    val scale: Vector2f
        get() = transformData.scale
    var rotation: Float
        get() = transformData.rotation
        set(value) { transformData.rotation = value }
    @JvmField var tint: MutableColor = MutableColor(1f, 1f, 1f, 1f)
    @JvmField var blend: BlendMode = BlendMode.NONE

    companion object {
        val of: (CharacterMetaData.() -> Unit) -> CharacterMetaData = { configure ->
            val instance = CharacterMetaData()
            instance.also(configure)
            instance
        }
    }
}