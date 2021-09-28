package com.inari.firefly.game.tile

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
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
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGrid
import com.inari.firefly.graphics.view.ViewChangeEvent
import com.inari.firefly.physics.animation.entity.EAnimation
import com.inari.firefly.physics.animation.timeline.IntTimelineProperty
import com.inari.firefly.physics.contact.EContact
import com.inari.util.Consumer
import com.inari.util.geom.PositionF
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

class TileMap private constructor() : SystemComponent(TileMap::class.simpleName!!) {

    @JvmField internal var viewRef = -1
    private val tileMapLayer = DynArray.of<MapLayer>(5, 5)
    private val tileDecorations = DynArray.of<MapLayer>(10, 50)
    @JvmField internal val compositeIds = DynArray.of<CompId>(10, 10)

    private var loaded = false
    private var active = false
    private var parallax = false

    private val parallaxListener: Consumer<ViewChangeEvent> = { event ->
        if (event.type == ViewChangeEvent.Type.ORIENTATION && event.id.instanceId == viewRef)
            updateParallaxLayer()
    }

    var view = ComponentRefResolver(View) { index -> viewRef = index }
    val withLayer: (MapLayer.() -> Unit) -> Unit = { configure ->
        val instance = MapLayer()
        instance.also(configure)
        tileMapLayer[instance.layerRef] = instance
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
        tileMapLayer.forEach { layer ->
            layer.tileSetMapping.forEach { tileMap ->
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

        tileMapLayer.forEach { layer ->
            activateTileSetForLayer(layer)
            buildTileGrid(layer)
            if (layer.parallaxFactorX >= 0f || layer.parallaxFactorY >= 0f)
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

        tileMapLayer.forEach { layer ->
            deleteTileGrid(layer)
            deactivateTileSetsForLayer(layer)
        }

        active = false
    }

    internal fun unload() {
        if (!loaded)
            return

        if (active)
            deactivate()

        // disposes all involved assets
        tileMapLayer.forEach { layer ->
            layer.tileSetMapping.forEach { tileMap ->
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
        tileMapLayer[layerRef]?.activeEntityRefs?.get(code) ?: -1

    private fun activateTileSetForLayer(layer: MapLayer) {

        layer.tileSetMapping.forEach { mapping ->
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
                    name = "tile_${tile.name}_view:${this@TileMap.viewRef}_layer:${layer.layerRef}"

                    withComponent(ETransform) {
                        view(this@TileMap.viewRef)
                        layer(layer.layerRef)
                    }

                    withComponent(ETile) {
                        sprite.instanceId = spriteId
                        tint = tile.tintColor ?: layer.tint ?: tint
                        blend = tile.blendMode ?: layer.blend ?: blend
                    }

                    withComponent(EMultiplier) {}

                    if (tile.hasContactComp) {
                        withComponent(EContact) {
                            if (tile.contactType !== com.inari.firefly.UNDEFINED_CONTACT_TYPE) {
                                bounds(0,0,
                                    tile.protoSprite.textureBounds.width,
                                    tile.protoSprite.textureBounds.height)
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
                                name = "tileAnim_${tile.name}_view:${this@TileMap.viewRef}_layer:${layer.layerRef}"
                                looping = true
                                timeline = tile.animationData!!.frames.toArray()
                                propertyRef = ETile.Property.SPRITE_REFERENCE
                            }
                        }
                    }
                }
                layer.activeEntityRefs[codeIndex] = entityId.instanceId
                codeIndex++
            }
        }
    }

    private fun buildTileGrid(layer: MapLayer) {
        layer.tileGridId = TileGrid.buildAndActivate {
            name = "tilegrid_${this@TileMap.name}_${layer.layerRef}"
            view(this@TileMap.viewRef)
            layer(layer.layerRef)
            if (layer.rendererRef >= 0)
                renderer(layer.rendererRef)
            gridWidth = layer.mapWidth
            gridHeight = layer.mapHeight
            cellWidth = layer.tileWidth
            cellHeight = layer.tileHeight
            position(layer.position)
            spherical = layer.spherical
        }

        val tileGrid = FFContext[TileGrid, layer.tileGridId]
        val codeIterator = layer.mapCodes.iterator()
        var x = 0
        var y = 0
        while (codeIterator.hasNext()) {
            tileGrid[x, y] = layer.activeEntityRefs[codeIterator.nextInt()]
            x++
            if (x >= tileGrid.gridWidth) {
                y++
                x = 0
            }
        }
    }

    private fun deactivateTileSetsForLayer(layer: MapLayer) {
        val iterator = layer.activeEntityRefs.iterator()
        while (iterator.hasNext())
            FFContext.delete(Entity, iterator.nextInt())

        layer.activeEntityRefs.clear()
    }

    private fun deleteTileGrid(layer: MapLayer) {
        FFContext.delete(TileGrid, layer.tileGridId)
    }

    private fun updateParallaxLayer() {
        val viewPos = FFContext[View, viewRef].worldPosition

        tileMapLayer.forEach { mapLayer ->
            if (mapLayer.parallaxFactorX != 0.0f || mapLayer.parallaxFactorY != 0.0f) {
                FFContext[TileGrid, mapLayer.tileGridId].position(
                    -viewPos.x * mapLayer.parallaxFactorX,
                    -viewPos.y * mapLayer.parallaxFactorY)
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
    class MapLayer {

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
        @JvmField var parallaxFactorX = 0.0f
        @JvmField var parallaxFactorY = 0.0f
        @JvmField var position: PositionF = PositionF(0.0f, 0.0f)
        @JvmField var spherical: Boolean = false
        @JvmField var blend = BlendMode.NORMAL_ALPHA
        @JvmField var tint = MutableColor(1f, 1f, 1f, 1f)
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

        var tileSetAsset = ComponentRefResolver(Asset) { index -> tileSetAssetRef = index }
        var codeOffset = 1
    }

}