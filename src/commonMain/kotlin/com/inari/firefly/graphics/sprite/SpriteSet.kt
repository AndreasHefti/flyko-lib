package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BindingIndex
import com.inari.firefly.core.api.NULL_BINDING_INDEX
import com.inari.firefly.core.api.SpriteData
import com.inari.util.NO_NAME
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class SpriteTemplate internal constructor(): SpriteData {

    @JvmField var name = NO_NAME
    override val textureBounds = Vector4i()
    override var hFlip = false
    override var vFlip = false

    @JvmField var spriteIndex = NULL_BINDING_INDEX
    override var textureIndex = NULL_BINDING_INDEX
        internal set

    fun reset() {
        spriteIndex = NULL_BINDING_INDEX
        textureIndex = NULL_BINDING_INDEX
        textureBounds(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
        hFlip = false
        vFlip = true
    }
}

@ComponentDSL
class SpriteFrame {

    @JvmField var interval: Long = 0
    @JvmField var sprite: SpriteTemplate = SpriteTemplate()

    val spriteTemplate: (SpriteTemplate.() -> Unit) -> Unit = { configure ->
        val sprite = SpriteTemplate()
        sprite.also(configure)
        this.sprite = sprite
    }
}

class SpriteSet private constructor(): Asset(SpriteSet) {

    @JvmField val textureRef = CReference(Texture)

    protected val spriteData = DynArray.of<SpriteTemplate>()
    val sprites: DynArrayRO<SpriteTemplate>
        get() = spriteData

    override fun assetIndex(at: Int) =
        spriteData[at]?.spriteIndex ?: NULL_BINDING_INDEX

    fun assetIndex(name: String): BindingIndex {
        if (name == NO_NAME)
            return NULL_BINDING_INDEX

        return spriteData
            .first { name == it.name }.spriteIndex
    }

    fun withSpriteTemplate(spriteTemplate: SpriteTemplate) = spriteData.add(spriteTemplate)
    fun withSpriteTemplate(builder: SpriteTemplate.() -> Unit) {
        val s = SpriteTemplate()
        s.also(builder)
        spriteData.add(s)
    }

    override fun load() {
        super.load()
        val textureIndex = resolveAssetIndex(textureRef.targetKey)
        for (i in 0 until spriteData.capacity) {
            val sprite = spriteData[i] ?: continue
            sprite.textureIndex = textureIndex
            sprite.spriteIndex = Engine.graphics.createSprite(sprite)
        }
    }

    override fun dispose() {
        for (i in 0 until spriteData.capacity) {
            val sprite = spriteData[i] ?: continue
            Engine.graphics.disposeSprite(sprite.spriteIndex)
            sprite.spriteIndex = NULL_BINDING_INDEX
        }
        super.dispose()
    }

    companion object : ComponentSubTypeBuilder<Asset, SpriteSet>(Asset,"SpriteSet") {
        override fun create() = SpriteSet()
    }
}

