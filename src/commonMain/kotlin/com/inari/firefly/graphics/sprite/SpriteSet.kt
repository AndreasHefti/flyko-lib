package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.SpriteData
import com.inari.firefly.physics.animation.IntFrameAnimation
import com.inari.util.NO_NAME
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class SpriteTemplate internal constructor(): SpriteData {

    @JvmField var name: String = NO_NAME
    override val textureBounds: Vector4i = Vector4i()
    override var hFlip: Boolean = false
    override var vFlip: Boolean = false

    var spriteIndex: Int = -1
        internal set
    override var textureIndex: Int = -1
        internal set

    fun reset() {
        spriteIndex = -1
        textureIndex = -1
        textureBounds(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
        hFlip = false
        vFlip = true
    }
}

@ComponentDSL
class SpriteFrame : IntFrameAnimation.IntFrame {

    var interval: Long = 0
    var sprite: SpriteTemplate = SpriteTemplate()

    val spriteTemplate: (SpriteTemplate.() -> Unit) -> Unit = { configure ->
        val sprite = SpriteTemplate()
        sprite.also(configure)
        this.sprite = sprite
    }

    override val timeInterval: Long
        get() { return interval }

    override val value: Int
        get() = sprite.spriteIndex

    companion object {
        val of: (SpriteFrame.() -> Unit) -> SpriteFrame = { configure ->
            val instance = SpriteFrame()
            instance.also(configure)
            instance
        }
    }
}

open class SpriteSet protected constructor(): Asset(SpriteSet) {

    @JvmField val textureRef = CReference(Texture)

    protected val spriteData = DynArray.of<SpriteTemplate>()
    val sprites: DynArrayRO<SpriteTemplate>
        get() = spriteData

    override fun setParentComponent(key: ComponentKey) {
        super.setParentComponent(key)
        textureRef(key)
    }

    override fun assetIndex(at: Int) =
        spriteData[at]?.spriteIndex ?: -1

    fun assetIndex(name: String): Int {
        if (name == NO_NAME)
            return -1

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
            sprite.spriteIndex = -1
        }
    }

    companion object :  ComponentSubTypeSystem<Asset, SpriteSet>(Asset, "SpriteSet") {
        override fun create() = SpriteSet()
    }
}

