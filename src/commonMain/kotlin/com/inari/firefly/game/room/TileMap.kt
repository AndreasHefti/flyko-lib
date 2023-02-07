package com.inari.firefly.game.room

import com.inari.firefly.core.*
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGrid
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.IntFrameAnimation
import com.inari.firefly.physics.animation.IntFrameAnimationControl
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_CONTACT_TYPE
import com.inari.util.*
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField
import kotlin.math.floor

open class TileMap : Component(TileMap) {

    @JvmField var viewRef = CReference(View)
    @JvmField var autoCreateLayer = true

    private val tileMapLayerData = DynArray.of<TileMapLayerData>(5, 5)
    private var parallax = false

    fun withTileSet(configure: (TileSet.() -> Unit)): ComponentKey
        = TileSet.build(configure)

    val withTileLayer: (TileMapLayerData.() -> Unit) -> Unit = { configure ->
        val instance = TileMapLayerData()
        instance.also(configure)
        tileMapLayerData + instance
    }

    override fun load() {
        super.load()

        // check view reference is set and valid
        if (!viewRef.exists)
            throw IllegalStateException("No View reference exists")

        // check or create needed layer for view
        val iter = tileMapLayerData.iterator()
        while (iter.hasNext()) {
            val lData = iter.next()
            if (autoCreateLayer && !lData.layerRef.exists) {
                Layer {
                    name = lData.layerRef.targetKey.name
                    this.viewRef(this@TileMap.viewRef)
                    position(lData.position)
                }
            } else if (!lData.layerRef.exists)
                throw IllegalStateException("No Layer with name ${lData.layerRef.targetKey.name} exists")
        }
    }

    override fun activate() {
        super.activate()
        tileMapLayerData.forEach { layerData ->
            // activate layer first
            Layer.activate(layerData.layerRef.refIndex)

            layerData.tileGridData.forEach { buildTileGrid(layerData, it) }
            activateTileSetForLayer(layerData)
            layerData.tileGridData.forEach { fillTileGrid(layerData, it) }

            if (layerData.parallaxFactorX >= 0f || layerData.parallaxFactorY >= 0f)
                parallax = true
        }

        if (parallax)
            Engine.registerListener(View.VIEW_CHANGE_EVENT_TYPE, parallaxListener)
    }

    override fun deactivate() {
        if (parallax)
            Engine.disposeListener(View.VIEW_CHANGE_EVENT_TYPE, parallaxListener)
        parallax = false

        tileMapLayerData.forEach { data ->
            data.tileGridData.forEach { deleteTileGrid(it) }
            deactivateTileSets()
        }
        super.deactivate()
    }

    fun getTileEntityIndex(layerName: String, code: Int): Int =
        getTileEntityIndex(Layer[layerName].index, code)
    fun getTileEntityIndex(layerId: Int, code: Int): Int =
        tileMapLayerData[layerId]?.entityCodeMapping?.get(code) ?: NULL_COMPONENT_INDEX

    private fun activateTileSetForLayer(layerData: TileMapLayerData) {

        layerData.tileGridData.forEach { tileGridData ->
            tileGridData.tileSetMapping.forEach { mapping ->
                var codeIndex = mapping.codeOffset
                val tileSet = TileSet[mapping.tileSetIndex]
                if (!tileSet.active)
                    TileSet.activate(mapping.tileSetIndex)

                var it = 0
                while (it < tileSet.tiles.capacity) {
                    val tile = tileSet.tiles[it++] ?: continue

                    val spriteId = tile.spriteTemplate.spriteIndex
                    if (spriteId < 0)
                        throw IllegalStateException("Missing sprite id for tile: ${tile.name} in TileSet: ${tileSet.name}")

                    val entityId = Entity {
                        autoActivation = true
                        if (tile.name != NO_NAME)
                            name = "${tile.name}_view:${this@TileMap.viewRef.refIndex}_layer:${layerData.layerRef.refIndex}"

                        withComponent(ETransform) {
                            viewRef(this@TileMap.viewRef)
                            layerRef(layerData.layerRef)
                        }

                        withComponent(ETile) {
                            spriteIndex = spriteId
                            tintColor(tile.tintColor ?: layerData.tint)
                            blendMode = tile.blendMode ?: layerData.blend
                            tileGridRef(tileGridData.tileGridIndex)
                        }

                        withComponent(EMultiplier) {}

                        if (tile.hasContactComp) {
                            withComponent(EContact) {
                                if (tile.contactType !== UNDEFINED_CONTACT_TYPE) {
                                    contactBounds(
                                        0, 0,
                                        tile.spriteTemplate.textureBounds.width,
                                        tile.spriteTemplate.textureBounds.height
                                    )
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
                                    animationController(IntFrameAnimationControl)
                                }
                            }
                        }
                    }

                    if (layerData.entityCodeMapping[codeIndex] >= 0)
                        throw IllegalStateException("Entity code mapping already exists, please check! code: $codeIndex")

                    layerData.entityCodeMapping[codeIndex] = entityId.componentIndex
                    codeIndex++
                }
            }
        }
    }

    private fun buildTileGrid(layerData: TileMapLayerData, gridData: TileMapGridData) {
        gridData.tileGridIndex = TileGrid {
            autoActivation = true
            //name = "tilegrid_${this@TileMap.name}_${layerData.layerIndex}"
            viewRef(this@TileMap.viewRef)
            layerRef(layerData.layerRef)
            if (gridData.renderer != null)
                renderer = gridData.renderer!!
            gridWidth = gridData.mapWidth
            gridHeight = gridData.mapHeight
            cellWidth = gridData.tileWidth
            cellHeight = gridData.tileHeight
            position(gridData.position)
            spherical = gridData.spherical
        }.componentIndex
    }

    private fun fillTileGrid(layerData: TileMapLayerData, gridData: TileMapGridData) {
        val tileGrid = TileGrid[gridData.tileGridIndex]
        val codeIterator = gridData.mapCodes.iterator()
        var x = 0
        var y = 0
        while (codeIterator.hasNext()) {
            tileGrid[x, y] = layerData.entityCodeMapping[codeIterator.nextInt()]
            x++
            if (x >= tileGrid.gridWidth) {
                y++
                x = 0
            }
        }
    }

    private fun deactivateTileSets() {
        tileMapLayerData.forEach { mapLayer ->
            val iterator = mapLayer.entityCodeMapping.iterator()
            while (iterator.hasNext())
                Entity.delete(iterator.nextInt())

            mapLayer.entityCodeMapping.clear()
        }
    }

    private fun deleteTileGrid(gridData: TileMapGridData) =
        TileGrid.delete(gridData.tileGridIndex)

    private val parallaxListener: (View.ViewChangeEvent) -> Unit = { event ->
        if (event.type == View.ViewChangeEvent.Type.ORIENTATION && event.viewIndex == viewRef.refIndex)
            updateParallaxLayer(event.pixelPerfect)
    }

    private fun updateParallaxLayer(pixelPerfect: Boolean) {
        val viewPos = View[viewRef].worldPosition

        tileMapLayerData.forEach { mapLayer ->
            if (mapLayer.parallaxFactorX != ZERO_FLOAT || mapLayer.parallaxFactorY != ZERO_FLOAT) {
                Layer[mapLayer.layerRef].position(
                    if (pixelPerfect) floor(-viewPos.x * mapLayer.parallaxFactorX) else -viewPos.x * mapLayer.parallaxFactorX,
                    if (pixelPerfect) floor(-viewPos.y * mapLayer.parallaxFactorY) else -viewPos.y * mapLayer.parallaxFactorY)
            }
        }
    }

    companion object : ComponentSystem<TileMap>("TileMap") {
        override fun allocateArray(size: Int): Array<TileMap?> = arrayOfNulls(size)
        override fun create() = TileMap()

        private val viewListener: ComponentEventListener = { key, type ->
            when(type) {
                ComponentEventType.DELETED ->  TileMap.forEachDo { tileMap ->
                    if (tileMap.viewRef.refIndex == key.componentIndex)
                        TileMap.delete(tileMap.index)
                }
                else -> DO_NOTHING
            }
        }

        init {
            View.registerComponentListener(viewListener)
        }

        fun getTileEntityIndex(tileMapName: String, layerName: String, code: Int): Int =
            TileMap[tileMapName].getTileEntityIndex(layerName, code)
        fun getTileEntityIndex(tileMapIndex: Int, layerIndex: Int, code: Int): Int =
            TileMap[tileMapIndex].getTileEntityIndex(layerIndex, code)

    }
}