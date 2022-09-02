package com.inari.firefly.game.tile

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.game.composite.Composite
import com.inari.firefly.game.composite.EComposite
import com.inari.firefly.game.json.*
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGrid
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.View.Companion.BASE_VIEW_KEY
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.firefly.graphics.view.ViewSystemRenderer
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.IntFrameAnimation
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.physics.contact.SimpleContactMap
import com.inari.util.*
import com.inari.util.collection.DynArray
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField
import kotlin.math.floor

open class TileMap protected constructor(): Composite(TileMap) {

    private val tileMapData = DynArray.of<TileMapData>(5, 5)
    private var parallax = false

    fun <C : ComponentNode> withTileSet(
        cBuilder: ComponentBuilder<C>,
        configure: (C.() -> Unit)): ComponentKey  = withChild(cBuilder, configure)

    val withTileLayer: (TileMapData.() -> Unit) -> Unit = { configure ->
        val instance = TileMapData()
        instance.also(configure)
        tileMapData[instance.layerIndex] = instance
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

    fun getTileEntityIndex(code: Int, layerKey: ComponentKey): Int =
        getTileEntityIndex(code, layerKey.instanceIndex)
    fun getTileEntityIndex(code: Int, viewLayer: ViewLayerAware): Int =
        getTileEntityIndex(code, viewLayer.layerIndex)
    fun getTileEntityIndex(code: Int, layerIndex: Int = 0): Int =
        tileMapData[layerIndex]?.entityCodeMapping?.get(code) ?: -1

    private fun activateTileSetForLayer(data: TileMapData) {

        data.tileSetMapping.forEach { mapping ->
            var codeIndex = mapping.codeOffset
            val tileSet = TileSet[mapping.tileSetIndex]
            var it = 0
            while (it < tileSet.tiles.capacity) {
                val tile = tileSet.tiles[it++] ?: continue

                val spriteId = tile.spriteTemplate.spriteIndex
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
                                    tile.spriteTemplate.textureBounds.width,
                                    tile.spriteTemplate.textureBounds.height)
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

    companion object :  ComponentSubTypeSystem<Composite, TileMap>(Composite, "TileMap") {
        override fun create() = TileMap()

        private val viewListener: ComponentEventListener = { index, type ->
            when(type) {
                ComponentEventType.DELETED ->  TileMap.forEachComponent { tileMap ->
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

        fun getTileEntityIndex(code: Int, viewIndex: Int = View[BASE_VIEW_KEY].index, layerIndex: Int = 0): Int =
            TileMap.findFirst {
               it.active && it.viewIndex == viewIndex
            }?.getTileEntityIndex(code, layerIndex) ?: -1
    }
}

class TiledJSONTileMap private constructor() : TileMap() {

    private var resource: () -> TiledTileMap = { throw RuntimeException() }

    @JvmField var encryptionKey: String? = null
    @JvmField var defaultBlend = BlendMode.NONE
    @JvmField var defaultTint = Vector4f(1f, 1f, 1f, 1f)
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { Engine.resourceService.loadJSONResource(value, TiledTileMap::class, encryptionKey) }
        }

    private val tileSetAssetToCodeOffsetMapping = mutableMapOf<String, Int>()

    override fun load() {
        if (!viewRef.exists)
            throw RuntimeException("Missing view reference")

        val tileMapJson = resource.invoke()

        // create TiledTileSetAssets
        val tileSets = tileMapJson.mappedProperties[PROP_NAME_TILE_SETS]?.stringValue?.split(COMMA)
            ?: throw RuntimeException("Missing tile sets definition")

        tileSetAssetToCodeOffsetMapping.clear()
        tileSets.forEachIndexed() { index, tilesetString ->
            val tileSetProps = tilesetString.split(COLON)
            val tileSetName = tileSetProps[0]
            val tileSetAssetName = "${tileSetName}$TILE_SET_ASSET_NAME_SUFFIX"
            val tilesetResource = tileSetProps[1]

            // activate existing tileset or create new one and activate
            if (!TileSet.exists(tileSetAssetName))
                TiledJSONTileSet.buildActive {
                    name = tileSetAssetName
                    resourceName = tilesetResource
                }
            TileSet.load(tileSetAssetName)
            tileSetAssetToCodeOffsetMapping[tileSetName] = tileMapJson.tilesets[index].firstgid
        }

        // build TileMap
        tileMapJson.layers.forEach { layerJson ->
            if (layerJson.type == PROP_NAME_TILE_LAYER)
                this.loadTileLayer(this, tileMapJson, layerJson)
            else if (layerJson.type == PROP_NAME_OBJECT_LAYER)
                this.loadObjectLayer(this, tileMapJson, layerJson)
        }
    }

    private fun loadObjectLayer(tileMap: TileMap, tileMapJson: TiledTileMap, layerJson: TiledLayer) {

        if (layerJson.mappedProperties.containsKey(PROP_NAME_ADD_CONTACT_MAP)) {
            val contactMap = SimpleContactMap.build {
                viewRef(this@TiledJSONTileMap.viewRef.targetKey)
                layerRef(layerJson.name)
            }
            tileMap.withChild(contactMap)
            //tileMap.compositeIds + contactMap
        }

        layerJson.objects!!.forEach { tiledObject ->

            if (tiledObject.type.startsWith(COMPOSITE_OBJECT_NAME_PREFIX)) {
                val typeName = tiledObject.type.split(COLON)

                tileMap.withChild(ComponentSystem.getComponentBuilder<Composite>(typeName[1])) {
                    name = tiledObject.name
                    viewRef(this@TiledJSONTileMap.viewIndex)
                    layerRef(layerJson.name)
                    position(tiledObject.x, tiledObject.y)
                    setAttribute("rotation", tiledObject.rotation.toString())
                    layerJson.properties.forEach { setAttribute(it.name, it.stringValue) }
                }

            } else if (tiledObject.type.startsWith(ENTITY_COMPOSITE_OBJECT_NAME_PREFIX)) {

                Entity.build {
                    tiledObject.mappedProperties[PROP_NAME_LOAD_TASK]?.also {
                        this@TiledJSONTileMap.onLoadTask(it.stringValue)
                    }
                    tiledObject.mappedProperties[PROP_NAME_ACTIVATION_TASK]?.also {
                        this@TiledJSONTileMap.onActivationTask(it.stringValue)
                    }
                    tiledObject.mappedProperties[PROP_NAME_DEACTIVATION_TASK]?.also {
                        this@TiledJSONTileMap.onDeactivationTask(it.stringValue)
                    }
                    tiledObject.mappedProperties[PROP_NAME_DISPOSE_TASK]?.also {
                        this@TiledJSONTileMap.onDisposeTask(it.stringValue)
                    }

                    name = tiledObject.name
                    withComponent(ETransform) {
                        viewRef(this@TiledJSONTileMap.viewIndex)
                        layerRef(layerJson.name)
                        position(tiledObject.x, tiledObject.y)
                        rotation = tiledObject.rotation
                    }
                    withComponent(EComposite) {
                        layerJson.properties.forEach {
                            setAttribute(it.name, it.stringValue)
                        }
                    }
                }

            } else {
                //throw RuntimeException("Unknown Tiles Object Type: ${tiledObject.type}")
            }
        }
    }

    private fun loadTileLayer(tileMap: TileMap, tileMapJson: TiledTileMap, layerJson: TiledLayer) {
        val layerTileSets = layerJson.mappedProperties[PROP_NAME_TILE_SETS]?.stringValue
            ?: throw RuntimeException("Missing tile sets for layer")

        tileMap.withTileLayer {
            position(layerJson.x + layerJson.offsetx, layerJson.y + layerJson.offsety)
            tileWidth = tileMapJson.tilewidth
            tileHeight = tileMapJson.tileheight
            mapWidth = layerJson.mappedProperties[PROP_NAME_WIDTH]?.intValue ?: tileMapJson.width
            mapHeight = layerJson.mappedProperties[PROP_NAME_HEIGHT]?.intValue ?: tileMapJson.height
            parallaxFactorX = layerJson.parallaxx - 1
            parallaxFactorY = layerJson.parallaxy - 1
            layer(layerJson.name)
            mapCodes = layerJson.data!!
            spherical = tileMapJson.infinite
            if (layerJson.tintcolor.isNotEmpty())
                tint = GeomUtils.colorOf(layerJson.tintcolor)
            if (layerJson.opacity < 1.0f)
                tint.a = layerJson.opacity
            if (PROP_NAME_BLEND in layerJson.mappedProperties)
                blend = BlendMode.valueOf(layerJson.mappedProperties[PROP_NAME_BLEND]?.stringValue!!)
            if (PROP_NAME_RENDERER in layerJson.mappedProperties)
                renderer = ViewSystemRenderer.byName(layerJson.mappedProperties[PROP_NAME_RENDERER]?.stringValue!!)

            // define tile sets for this map layer
            val tileSetNames = layerTileSets.split(COMMA)
            tileSetNames.forEach { tileSetName ->
                withTileSetMapping {
                    tileSetRef("${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}")
                    codeOffset = this@TiledJSONTileMap.tileSetAssetToCodeOffsetMapping[tileSetName] ?: 0
                }
            }
        }
    }

    override fun dispose() {
        // dispose all tile set assets
        tileSetAssetToCodeOffsetMapping.keys.forEach{ tileSetName ->
            Asset.deactivate("${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}")
        }
        tileSetAssetToCodeOffsetMapping.clear()
    }

    companion object :  ComponentSubTypeSystem<Composite, TiledJSONTileMap>(Composite, "TiledJSONTileMap") {
        override fun create() = TiledJSONTileMap()
    }
}