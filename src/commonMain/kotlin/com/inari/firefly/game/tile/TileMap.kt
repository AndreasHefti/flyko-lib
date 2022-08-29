package com.inari.firefly.game.tile

import com.inari.firefly.core.*
import com.inari.firefly.graphics.sprite.SpriteSet
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGrid
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.View.Companion.BASE_VIEW_KEY
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.IntFrameAnimation
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_CONTACT_TYPE
import com.inari.util.DO_NOTHING
import com.inari.util.ZERO_FLOAT
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField
import kotlin.math.floor

class TileMap  private constructor(): Component(TileMap) {

    private val tileMapData = DynArray.of<TileMapData>(5, 5)
    private val tileDecorations = DynArray.of<TileMapData>(10, 50)
    private var parallax = false

    @JvmField var viewRef = CReference(View)
    val viewIndex: Int
        get() = viewRef.targetKey.instanceIndex

    val withTileLayer: (TileMapData.() -> Unit) -> Unit = { configure ->
        val instance = TileMapData()
        instance.also(configure)
        tileMapData[instance.layerIndex] = instance
    }

    val withDecoration: (TileDecoration.() -> Unit) -> Unit = { configure ->
        val instance = TileDecoration()
        instance.also(configure)
        tileDecorations + instance
    }

    override fun load() {
        // load all involved assets (texture assets, tile set assets)
        tileMapData.forEach {
            it.tileSetMapping.forEach { tileSet ->
                Asset.load(tileSet.tileSetIndex)
                TileSet.load(tileSet.tileSetIndex)
            }
        }

        // TODO create inactive entities for decoration
    }

    override fun activate() {
        tileMapData.forEach { data ->
            buildTileGrid(data)
            activateTileSetForLayer(data)
            fillTileGrid(data)

            if (data.parallaxFactorX >= 0f || data.parallaxFactorY >= 0f)
                parallax = true
        }

        if (parallax)
            Engine.registerListener(View.VIEW_CHANGE_EVENT_TYPE, parallaxListener)
    }

    override fun deactivate() {
        if (parallax)
            Engine.disposeListener(View.VIEW_CHANGE_EVENT_TYPE, parallaxListener)
        parallax = false

        tileMapData.forEach { data ->
            deleteTileGrid(data)
            deactivateTileSetsForLayer(data)
        }
    }

    override fun dispose() {
        // disposes all involved assets
        tileMapData.forEach {
            it.tileSetMapping.forEach { tileMap ->
                Asset.deactivate(tileMap.tileSetIndex)
            }
        }
    }

    fun getTileEntityIndex(code: Int, layerKey: ComponentKey): Int =
        getTileEntityIndex(code, layerKey.instanceIndex)
    fun getTileEntityIndex(code: Int, viewLayer: ViewLayerAware): Int =
        getTileEntityIndex(code, viewLayer.layerIndex)
    fun getTileEntityIndex(code: Int, layerIndex: Int = 0): Int =
        tileMapData[layerIndex]?.entityCodeMapping?.get(code) ?: -1

    private fun activateTileSetForLayer(data: TileMapData) {

        data.tileSetMapping.forEach { mapping ->
            //val spriteSetAssetIndex = Asset[mapping.spriteSetIndex].assetIndex
            var codeIndex = mapping.codeOffset
            val tileSet = TileSet[mapping.tileSetIndex]

            if (tileSet.spriteSetKey == NO_COMPONENT_KEY)
                throw IllegalStateException("Missing sprite set asset for TileSet: ${tileSet.name}")

            SpriteSet.activate(tileSet.spriteSetKey)

            var it = 0
            while (it < tileSet.tileTemplates.capacity) {
                val tile = tileSet.tileTemplates[it++] ?: continue

                val spriteId = tile.protoSprite.assetIndex
                if (spriteId < 0)
                    throw IllegalStateException("Missing sprite id for tile: ${tile.name} in TileSet: ${tileSet.name}")

                val entityId = Entity.buildActive {
                    name = "tile_${tile.name}_view:${this@TileMap.viewIndex}_layer:${data.layerIndex}"

                    withComponent(ETransform) {
                        viewRef(this@TileMap.viewIndex)
                        layerRef(data.layerIndex)
                    }

                    withComponent(ETile) {
                        spriteIndex = spriteId
                        tintColor(tile.tintColor ?: data.tint)
                        blendMode = tile.blendMode ?: data.blend
                        tileGridRef(data.tileGridIndex)
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
                            withAnimation(IntFrameAnimation) {
                                animatedProperty = ETile.PropertyAccessor.SPRITE
                                looping = true
                                timeline = tile.animationData!!.frames.toArray()
                            }
                        }
                    }
                }
                data.entityCodeMapping[codeIndex] = entityId.instanceIndex
                codeIndex++
            }
        }
    }

    private fun buildTileGrid(data: TileMapData) {
        data.tileGridIndex = TileGrid.buildActive {
            name = "tilegrid_${this@TileMap.name}_${data.layerIndex}"
            viewRef(this@TileMap.viewIndex)
            layerRef(data.layerIndex)
            if (data.renderer != null)
                renderer = data.renderer!!
            gridWidth = data.mapWidth
            gridHeight = data.mapHeight
            cellWidth = data.tileWidth
            cellHeight = data.tileHeight
            position(data.position)
            spherical = data.spherical
        }.instanceIndex
    }

    private fun fillTileGrid(data: TileMapData) {
        val tileGrid = TileGrid[data.tileGridIndex]
        val codeIterator = data.mapCodes.iterator()
        var x = 0
        var y = 0
        while (codeIterator.hasNext()) {
            tileGrid[x, y] = data.entityCodeMapping[codeIterator.nextInt()]
            x++
            if (x >= tileGrid.gridWidth) {
                y++
                x = 0
            }
        }
    }

    private fun deactivateTileSetsForLayer(data: TileMapData) {
        val iterator = data.entityCodeMapping.iterator()
        while (iterator.hasNext())
            Entity.delete(iterator.nextInt())

        data.entityCodeMapping.clear()
    }

    private fun deleteTileGrid(data: TileMapData) =
        TileGrid.delete(data.tileGridIndex)

    private val parallaxListener: (View.ViewChangeEvent) -> Unit = { event ->
        if (event.type == View.ViewChangeEvent.Type.ORIENTATION && event.viewIndex == viewIndex)
            updateParallaxLayer(event.pixelPerfect)
    }

    private fun updateParallaxLayer(pixelPerfect: Boolean) {
        val viewPos = View[viewIndex].worldPosition

        tileMapData.forEach { mapLayer ->
            if (mapLayer.parallaxFactorX != ZERO_FLOAT || mapLayer.parallaxFactorY != ZERO_FLOAT) {
                TileGrid[mapLayer.tileGridIndex].position(
                    if (pixelPerfect) floor(-viewPos.x * mapLayer.parallaxFactorX) else -viewPos.x * mapLayer.parallaxFactorX,
                    if (pixelPerfect) floor(-viewPos.y * mapLayer.parallaxFactorY) else -viewPos.y * mapLayer.parallaxFactorY)
            }
        }
    }

    override val componentType = Companion
    companion object : ComponentSystem<TileMap>("TileMap") {
        override fun allocateArray(size: Int): Array<TileMap?> = arrayOfNulls(size)
        override fun create() = TileMap()

        private val viewListener: ComponentEventListener = { index, type ->
            when(type) {
                ComponentEventType.DELETED ->  COMPONENT_MAPPING.forEach { tileMap ->
                    if (tileMap.viewIndex == index)
                        TileMap.delete(tileMap.index)
                }
                else -> DO_NOTHING
            }
        }

        init {
            View.registerComponentListener(viewListener)
        }

        fun getTileEntityIndex(code: Int, view: ComponentKey, layer: ComponentKey): Int =
            getTileEntityIndex(code, view.instanceIndex, layer.instanceIndex)

        fun getTileEntityIndex(code: Int, viewLayer: ViewLayerAware): Int =
            getTileEntityIndex(code, viewLayer.viewIndex, viewLayer.layerIndex)

        fun getTileEntityIndex(code: Int, viewIndex: Int = View[BASE_VIEW_KEY].index, layerIndex: Int = 0): Int {
            //var result = -1
            var i = ACTIVE_COMPONENT_MAPPING.nextSetBit(0)
            while (i >= 0) {
                val map = COMPONENT_MAPPING[i]
                if (map?.viewIndex == viewIndex)
                    return map.getTileEntityIndex(code, layerIndex)
                i = ACTIVE_COMPONENT_MAPPING.nextSetBit(i + 1)
            }
            return -1
        }
    }
}