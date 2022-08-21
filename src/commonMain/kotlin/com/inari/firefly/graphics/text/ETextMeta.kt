package com.inari.firefly.graphics.text

import com.inari.firefly.core.ComponentDSL
import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.TransformData
import com.inari.firefly.core.api.TransformDataImpl
import com.inari.util.collection.DynArray
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

@ComponentDSL
class CharacterMetaData internal constructor(){

    @JvmField internal val transformData = TransformDataImpl()

    val position: Vector2f
        get() = transformData.position
    val pivot: Vector2f
        get() = transformData.pivot
    val scale: Vector2f
        get() = transformData.scale
    var rotation: Float
        get() = transformData.rotation
        set(value) { transformData.rotation = value }
    @JvmField var tint: Vector4f = Vector4f(1f, 1f, 1f, 1f)
    @JvmField var blend: BlendMode = BlendMode.NONE

    companion object {
        val of: (CharacterMetaData.() -> Unit) -> CharacterMetaData = { configure ->
            val instance = CharacterMetaData()
            instance.also(configure)
            instance
        }
    }
}

class ETextMeta private constructor() : EntityComponent(ETextMeta) {

    @JvmField internal val metaData: DynArray<CharacterMetaData> = DynArray.of()
    @JvmField var resolver: (Int) -> CharacterMetaData? = { index -> metaData[index] }

    fun withData (configure: CharacterMetaData.() -> Unit) {
        val data = CharacterMetaData()
        data.also(configure)
        metaData + data
    }

    override fun reset() {
        metaData.clear()
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<ETextMeta>("ETextMeta") {
        override fun create() = ETextMeta()
    }
}