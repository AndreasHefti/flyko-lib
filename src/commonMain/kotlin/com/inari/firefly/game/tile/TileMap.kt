package com.inari.firefly.game.tile

import com.inari.firefly.*
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynIntArray
import com.inari.firefly.asset.Asset
import com.inari.firefly.entity.EMultiplier
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.rendering.Renderer
import com.inari.firefly.graphics.sprite.SpriteSetAsset
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGrid
import com.inari.firefly.graphics.tile.TileGridSystem
import com.inari.firefly.graphics.view.ViewChangeEvent
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.TimelineIntAnimation
import com.inari.firefly.physics.contact.EContact
import com.inari.util.Consumer
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField
import kotlin.math.floor

class TileMap private constructor() : SystemComponent(TileMap::class.simpleName!!) {

    @JvmField internal var viewRef = -1
    private val tileMapData = DynArray.of<TileMapData>(5, 5)
    private val tileDecorations = DynArray.of<TileMapData>(10, 50)
    @JvmField internal val compositeIds = DynArray.of<CompId>(10, 10)

    private var loaded = false
    private var active = false
    private var parallax = false

    private val parallaxListener: Consumer<ViewChangeEvent> = { event ->
        if (event.type == ViewChangeEvent.Type.ORIENTATION && event.id.instanceId == viewRef)
            updateParallaxLayer(event.pixelPerfect)
    }

    var view = ComponentRefResolver(View) { index -> viewRef = index }
    val withLayer: (TileMapData.() -> Unit) -> Unit = { configure ->
        val instance = TileMapData()
        instance.also(configure)
        tileMapData[instance.layerRef] = instance
    }

    val withDecoration: (TileDecoration.() -> Unit) -> Unit = { configure ->
        val instance = TileDecoration()
        instance.also(configure)
        tileDecorations + instance
    }

    internal fun load() {
        if (loaded)
            return

        // load all involved assets (texture assets, tile set assets)
        tileMapData.forEach {
            it.tileSetMapping.forEach { tileMap ->
                FFContext.activate(Asset, tileMap.tileSetAssetRef)
            }
        }

        // TODO create inactive entities for decoration

        loaded = true
    }

    internal fun activate() {
        if (active)
            return

        if (!loaded)
            load()

        tileMapData.forEach { data ->
            buildTileGrid(data)
            activateTileSetForLayer(data)
            fillTileGrid(data)

            if (data.parallaxFactorX >= 0f || data.parallaxFactorY >= 0f)
                parallax = true
        }

        if (!compositeIds.isEmpty)
            FFContext.activateAll(compositeIds)

        if (parallax)
            FFContext.registerListener(ViewChangeEvent, parallaxListener)

        active = true
    }

    internal fun deactivate() {
        if (!active)
            return

        if (parallax)
            FFContext.disposeListener(ViewChangeEvent, parallaxListener)

        parallax = false
        if (!compositeIds.isEmpty)
            FFContext.deactivateAll(compositeIds)

        tileMapData.forEach { data ->
            deleteTileGrid(data)
            deactivateTileSetsForLayer(data)
        }

        TileGridSystem

        active = false
    }

    internal fun unload() {
        if (!loaded)
            return

        if (active)
            deactivate()

        // disposes all involved assets
        tileMapData.forEach {
            it.tileSetMapping.forEach { tileMap ->
                FFContext.deactivate(Asset, tileMap.tileSetAssetRef)
            }
        }

        if (!compositeIds.isEmpty)
            FFContext.deleteAll(compositeIds)
        compositeIds.clear()

        loaded = false
    }

    fun getTileEntityRef(code: Int, layer: CompId): Int =
        getTileEntityRef(code, layer.index)

    fun getTileEntityRef(code: Int, viewLayer: ViewLayerAware): Int =
        getTileEntityRef(code, viewLayer.layerIndex)

    fun getTileEntityRef(code: Int, layerRef: Int = 0): Int =
        tileMapData[layerRef]?.activeEntityRefs?.get(code) ?: -1

    private fun activateTileSetForLayer(data: TileMapData) {

        data.tileSetMapping.forEach { mapping ->
            val tileSetId = FFContext[Asset, mapping.tileSetAssetRef].instanceId
            var codeIndex = mapping.codeOffset
            val tileSet = FFContext[TileSet, tileSetId]

            if (tileSet.spriteSetAssetId == NO_COMP_ID)
                throw IllegalStateException("Missing sprite set asset for TileSet: ${tileSet.name}")

            FFContext.activate(tileSet.spriteSetAssetId)

            var it = 0
            while (it < tileSet.tileTemplates.capacity) {
                val tile = tileSet.tileTemplates[it++] ?: continue

                val spriteId = tile.protoSprite.instanceId
                if (spriteId < 0)
                    throw IllegalStateException("Missing sprite id for tile: ${tile.name} in TileSet: ${tileSet.name}")

                val entityId = Entity.buildAndActivate {
                    name = "tile_${tile.name}_view:${this@TileMap.viewRef}_layer:${data.layerRef}"

                    withComponent(ETransform) {
                        view(this@TileMap.viewRef)
                        layer(data.layerRef)
                    }

                    withComponent(ETile) {
                        sprite.instanceId = spriteId
                        tint = tile.tintColor ?: data.tint
                        blend = tile.blendMode ?: data.blend
                        tileGrid(data.tileGridId)
                    }

                    withComponent(EMultiplier) {}

                    if (tile.hasContactComp) {
                        withComponent(EContact) {
                            if (tile.contactType !== UNDEFINED_CONTACT_TYPE) {
                                contactBounds(0,0,
                                    tile.protoSprite.textureBounds.width,
                                    tile.protoSprite.textureBounds.height)
                                contactType = tile.contactType
                                material = tile.material
                                contactBounds(tile.contactMask)
                            }
                            material = tile.material
                        }
                    }

                    if (tile.animationData != null) {
                        withComponent(EAnimation) {
                            withAnimated<Int> {
                                animatedProperty = ETile.Property.SPRITE_REFERENCE
                                looping = true
                                withActiveAnimation(TimelineIntAnimation) {
                                    timeline = tile.animationData!!.frames.toArray()
                                }
                            }
                        }
                    }
                }
                data.activeEntityRefs[codeIndex] = entityId.instanceId
                codeIndex++
            }
        }
    }

    private fun buildTileGrid(data: TileMapData) {
        data.tileGridId = TileGrid.buildAndActivate {
            name = "tilegrid_${this@TileMap.name}_${data.layerRef}"
            view(this@TileMap.viewRef)
            layer(data.layerRef)
            if (data.rendererRef >= 0)
                renderer(data.rendererRef)
            gridWidth = data.mapWidth
            gridHeight = data.mapHeight
            cellWidth = data.tileWidth
            cellHeight = data.tileHeight
            position(data.position)
            spherical = data.spherical
        }
    }

    private fun fillTileGrid(data: TileMapData) {
        val tileGrid = FFContext[TileGrid, data.tileGridId]
        val codeIterator = data.mapCodes.iterator()
        var x = 0
        var y = 0
        while (codeIterator.hasNext()) {
            tileGrid[x, y] = data.activeEntityRefs[codeIterator.nextInt()]
            x++
            if (x >= tileGrid.gridWidth) {
                y++
                x = 0
            }
        }
    }

    private fun deactivateTileSetsForLayer(data: TileMapData) {
        val iterator = data.activeEntityRefs.iterator()
        while (iterator.hasNext())
            FFContext.delete(Entity, iterator.nextInt())

        data.activeEntityRefs.clear()
    }

    private fun deleteTileGrid(data: TileMapData) {
        FFContext.delete(TileGrid, data.tileGridId)
    }

    private fun updateParallaxLayer(pixelPerfect: Boolean) {
        val viewPos = FFContext[View, viewRef].worldPosition

        tileMapData.forEach { mapLayer ->
            if (mapLayer.parallaxFactorX != ZERO_FLOAT || mapLayer.parallaxFactorY != ZERO_FLOAT) {
                FFContext[TileGrid, mapLayer.tileGridId].position(
                    if (pixelPerfect) floor(-viewPos.x * mapLayer.parallaxFactorX) else -viewPos.x * mapLayer.parallaxFactorX,
                    if (pixelPerfect) floor(-viewPos.y * mapLayer.parallaxFactorY) else -viewPos.y * mapLayer.parallaxFactorY)
            }
        }
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<TileMap>(TileMap::class) {
        override fun createEmpty() = TileMap()
    }

    @ComponentDSL
    class TileDecoration {
        @JvmField internal var layerRef = 0
        @JvmField internal var rendererRef = -1

        @JvmField var tileName = NO_NAME
        @JvmField var renderer = ComponentRefResolver(Renderer) { index-> rendererRef = index }
        @JvmField var layer = ComponentRefResolver(Layer) { index -> layerRef = index }
    }

    @ComponentDSL
    class TileMapData {

        @JvmField internal val activeEntityRefs = DynIntArray(100, -1, 100)
        @JvmField internal val tileSetMapping = DynArray.of<TileSetAssetMapping>(5, 5)
        @JvmField internal var layerRef = 0
        @JvmField internal var rendererRef = -1
        @JvmField internal var tileGridId = NO_COMP_ID

        @JvmField var renderer = ComponentRefResolver(Renderer) { index-> rendererRef = index }
        @JvmField var mapWidth = 0
        @JvmField var mapHeight = 0
        @JvmField var tileWidth = 0
        @JvmField var tileHeight = 0
        @JvmField var parallaxFactorX = ZERO_FLOAT
        @JvmField var parallaxFactorY = ZERO_FLOAT
        @JvmField var position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT)
        @JvmField var spherical: Boolean = false
        @JvmField var blend = BlendMode.NORMAL_ALPHA
        @JvmField var tint = Vector4f(1f, 1f, 1f, 1f)
        @JvmField var layer = ComponentRefResolver(Layer) { index -> layerRef = index }
        @JvmField var mapCodes: IntArray = intArrayOf()

        val withTileSet: (TileSetAssetMapping.() -> Unit) -> Unit = { configure ->
            val instance = TileSetAssetMapping()
            instance.also(configure)
            tileSetMapping.add(instance)
        }
    }

    class TileSetAssetMapping {
        @JvmField internal var tileSetAssetRef = -1

        var tileSetAsset = ComponentRefResolver(SpriteSetAsset) { index -> tileSetAssetRef = index }
        var codeOffset = 1
    }

}