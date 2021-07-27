package com.inari.firefly.graphics.tile.set

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.composite.Composite
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.ArrayAccessor
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.EMultiplier
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.sprite.SpriteSetAsset
import com.inari.firefly.graphics.text.FontAsset
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.animation.entity.EAnimation
import com.inari.firefly.physics.animation.timeline.IntTimelineProperty
import com.inari.firefly.physics.contact.ContactSystem
import com.inari.firefly.physics.contact.EContact
import com.inari.util.collection.DynArray
import com.inari.util.graphics.IColor
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

@ComponentDSL
class TileSet : Composite() {

    @JvmField internal var textureAssetRef: Int = -1
    private var spriteSetAssetId = NO_COMP_ID
    private var viewRef = -1
    private var layerRef = -1
    private val int_tiles = DynArray.of<ProtoTile>()
    private var active = false

    val texture = ComponentRefResolver(FontAsset) { index -> textureAssetRef = index }
    var view = ComponentRefResolver(View) { index -> viewRef = index }
    var layer = ComponentRefResolver(Layer) { index -> layerRef = index }
    var blend: BlendMode? = null
    var tint: MutableColor? = null

    val tile: (ProtoTile.() -> Unit) -> ProtoTile = { configure ->
        val tile = ProtoTile()
        tile.also(configure)
        int_tiles.add(tile)
        tile
    }
    val tiles = ArrayAccessor(int_tiles)

    override fun load() {
        if (loaded)
            return

        spriteSetAssetId = SpriteSetAsset.build {
            name = super.name
            texture(this@TileSet.textureAssetRef)
            this@TileSet.int_tiles.forEach {
                if (it.animationData != null)
                    spriteData.addAll(it.animationData!!.sprites.values)
                if (it.spriteData != null)
                    spriteData.add(it.spriteData!!)
            }
        }
    }

    override fun activate() {
        if (active)
            return

        if (!loaded)
            load()

        if (spriteSetAssetId == NO_COMP_ID)
            throw IllegalStateException()

        FFContext.activate(spriteSetAssetId)
        if (activateInternal())
            active = true
    }

    private fun activateInternal(): Boolean {
        if (layerRef == -1)
            return false

        var it = 0
        while (it < int_tiles.capacity) {
            val tile = int_tiles[it++] ?: continue

            if (tile.spriteData == null)
                continue

            val spriteId = tile.spriteData!!.instanceId
            if (spriteId < 0)
                return false

            val entityId = Entity.buildAndActivate {
                name = tile.name
                withComponent(ETransform) {
                    view(this@TileSet.viewRef)
                    layer(this@TileSet.layerRef)
                }
                withComponent(ETile) {
                    sprite.instanceId = spriteId
                    tint = tile.tintColor ?: this@TileSet.tint ?: tint
                    blend = tile.blendMode ?: this@TileSet.blend ?: blend
                }
                withComponent(EMultiplier) {

                }

                if (tile.hasContactComp) {
                    withComponent(EContact) {
                        if (tile.contactType !== UNDEFINED_CONTACT_TYPE) {
                            bounds(0,0,
                                tile.spriteData!!.textureBounds.width,
                                tile.spriteData!!.textureBounds.height)
                            contactType = tile.contactType
                            material = tile.material
                            mask = tile.contactMask ?: mask
                        }
                        material = tile.material
                    }
                }

                if (tile.animationData != null) {
                    withComponent(EAnimation) {
                        withActiveAnimation(IntTimelineProperty) {
                            looping = true
                            timeline = tile.animationData!!.frames.toArray()
                            propertyRef = ETile.Property.SPRITE_REFERENCE
                        }
                    }
                }
            }

            tile.entityRef = entityId.instanceId
            TileSetContext.addActiveTileEntityId(entityId.instanceId, layerRef)
        }
        return true
    }

    override fun deactivate() {
        if (!active)
            return

        var i = 0
        while (i < int_tiles.capacity) {
            val tile = int_tiles[i++] ?: continue

            if (tile.entityRef < 0)
                continue

            deactivateTile(tile)
        }

        TileSetContext.updateActiveTileEntityRefs(layerRef)
        FFContext.deactivate(spriteSetAssetId)
        this.active = false
    }

    private fun deactivateTile(protoTile: ProtoTile) {
        TileSetContext.removeActiveTileEntityId(protoTile.entityRef, layerRef)
        FFContext.delete(Entity, protoTile.entityRef)
    }

    override fun unload() {
        FFContext.delete(spriteSetAssetId)
        spriteSetAssetId = NO_COMP_ID
    }

    companion object : SystemComponentSubType<Composite, TileSet>(Composite, TileSet::class) {
        override fun createEmpty() = TileSet()
    }
}