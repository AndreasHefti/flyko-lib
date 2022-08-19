package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.SpriteData
import com.inari.util.NO_NAME
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class SpriteSet private constructor(): Asset() {

    private val spriteData = DynArray.of<SpriteInfo>(30)
    private val tmpSpriteData = object : SpriteData {
        override var textureIndex: Int = -1
            internal set
        override val region: Vector4i = Vector4i(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
        override var isHorizontalFlip: Boolean = false
        override var isVerticalFlip: Boolean = false
        fun reset() {
            textureIndex = -1
            region(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
            isHorizontalFlip = false
            isVerticalFlip = true
        }
    }

    @JvmField val sprites: DynArrayRO<SpriteInfo> = spriteData
    @JvmField val textureRef = CReference(Texture)

    override fun notifyParent(comp: Component) {
        textureRef(comp.name)
    }

    fun withSpriteInfo(builder: SpriteInfo.() -> Unit) {
        val s = SpriteInfo()
        s.also(builder)
        spriteData.add(s)
    }

    override fun assetIndex(at: Int) =
        spriteData[at]?.assetIndex ?: -1

    fun assetIndex(name: String): Int {
        if (name == NO_NAME)
            return -1

        return spriteData
            .first { name == it.name }.assetIndex
    }

    override fun load() {
        with( Engine.graphics) {
            tmpSpriteData.textureIndex = resolveAssetIndex(textureRef.targetKey)
            for (i in 0 until spriteData.capacity) {
                val sprite = spriteData[i] ?: continue
                tmpSpriteData.region(sprite.textureBounds)
                tmpSpriteData.isHorizontalFlip = sprite.hFlip
                tmpSpriteData.isVerticalFlip = sprite.vFlip
                sprite.assetIndex = createSprite(tmpSpriteData)
            }
        }
        tmpSpriteData.reset()
    }

    override fun dispose() {
        with( Engine.graphics) {
            for (i in 0 until spriteData.capacity) {
                val sprite = spriteData[i] ?: continue
                disposeSprite(sprite.assetIndex)
                sprite.assetIndex = -1
            }
        }
    }

    companion object :  ComponentSubTypeSystem<Asset, SpriteSet>(Asset) {
        override fun create() = SpriteSet()
    }

    class SpriteInfo internal constructor() {

        var assetIndex: Int = -1
            internal set

        @JvmField var name: String = NO_NAME
        @JvmField val textureBounds: Vector4i = Vector4i()
        @JvmField var hFlip: Boolean = false
        @JvmField var vFlip: Boolean = false
    }
}