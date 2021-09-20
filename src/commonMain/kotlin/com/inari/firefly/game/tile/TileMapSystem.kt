package com.inari.firefly.game.tile

import com.inari.firefly.BASE_VIEW
import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.game.tile.TileUtils.createFullCircleBitMask_16
import com.inari.firefly.game.tile.TileUtils.createFullCircleBitMask_8
import com.inari.firefly.game.tile.TileUtils.createFullRhombusBitMask_16
import com.inari.firefly.game.tile.TileUtils.createFullRhombusBitMask_8
import com.inari.firefly.graphics.sprite.SpriteSetAsset
import com.inari.firefly.graphics.view.*
import com.inari.util.Consumer
import com.inari.util.aspect.Aspects
import com.inari.util.geom.BitMask
import com.inari.util.geom.Direction
import kotlin.jvm.JvmField

object TileMapSystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
        TileSet,
        TileMap)

    private val bitMaskCache: MutableMap<String, BitMask> = HashMap()

    val tileSets: ComponentMapRO<TileSet>
        get() = systemTileSet
    @JvmField val systemTileSet = ComponentSystem.createComponentMapping(
        TileSet,
        listener = { tileSet, action -> when (action) {
            CREATED -> initTileSet(tileSet)
            DELETED -> disposeTileSet(tileSet)
            else -> {}
        } }
    )

    val tileMaps: ComponentMapRO<TileMap>
        get() = systemTileMaps
    @JvmField val systemTileMaps = ComponentSystem.createComponentMapping(
        TileMap,
        activationMapping = true,
        listener = { tileMap, action -> when (action) {
            ACTIVATED -> tileMap.activate()
            DEACTIVATED -> tileMap.deactivate()
            DELETED -> tileMap.unload()
            else -> {}
        } }
    )

    private val viewListener: Consumer<ViewEvent> = { e ->
        when(e.type) {
            ViewEvent.Type.VIEW_DELETED ->  systemTileMaps.forEach { tileMap ->
                if (tileMap.viewRef == e.id.instanceId)
                    FFContext.deleteQuietly(tileMap.componentId)
            }
            else -> {}
        }
    }

    init {
        FFContext.registerListener(ViewEvent, viewListener)
        FFContext.loadSystem(this)
    }

    val loadTileMap = ComponentRefResolver(TileMap) { id -> systemTileMaps[id].load() }
    val activateTileMap = ComponentRefResolver(TileMap) { id -> FFContext.activate(TileMap, id) }
    val deactivateTileMap = ComponentRefResolver(TileMap) { id -> FFContext.deactivate(TileMap, id) }
    val unloadTileMap = ComponentRefResolver(TileMap) { id -> systemTileMaps[id].unload() }

    fun getTileEntityRef(code: Int, view: CompId, layer: CompId): Int =
        getTileEntityRef(code, view.index, layer.index)

    fun getTileEntityRef(code: Int, viewLayer: ViewLayerAware): Int =
        getTileEntityRef(code, viewLayer.viewIndex, viewLayer.layerIndex)

    fun getTileEntityRef(code: Int, viewRef: Int = ViewSystem.views[BASE_VIEW].index, layerRef: Int = 0): Int {
        var result = -1
        systemTileMaps.forEachActive { map ->
            if (map.viewRef == viewRef)
                result = map.getTileEntityRef(code, layerRef)
        }
        return result
    }

    fun createTileContactBitMask(
        formType: TileContactFormType,
        tileSizeType: TileSizeType = TileSizeType.EMPTY,
        tileOrientation: TileOrientation = TileOrientation.NONE,
        tileDirection: TileDirection = TileDirection.NONE,
        dimType: TileDimType = TileDimType.EIGHT): BitMask? {

        val cacheKey = "$formType $tileSizeType $tileOrientation $tileDirection $dimType"
        if (cacheKey in bitMaskCache)
            return bitMaskCache[cacheKey]

        val size = when (dimType) {
            TileDimType.EIGHT -> 8
            TileDimType.SIXTEEN -> 16
            TileDimType.THIRTY_TWO -> 32
        }
        val half = size / 2

        val result: BitMask? = when (formType) {
            TileContactFormType.QUAD, TileContactFormType.RECTANGLE -> when (tileSizeType) {
                TileSizeType.HALF -> when (tileDirection) {
                    TileDirection.NORTH -> BitMask(0, 0, size, size).setRegion(0, 0, size, half)
                    TileDirection.EAST -> BitMask(0, 0, size, size).setRegion(half, 0, half, size)
                    TileDirection.SOUTH ->  BitMask(0, 0, size, size).setRegion(0, half, size, half)
                    TileDirection.WEST -> BitMask(0, 0, size, size).setRegion(0, 0, half, size)
                    else -> null
                }
                TileSizeType.QUARTER -> when (tileDirection) {
                    TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setRegion(0, 0, half, half)
                    TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setRegion(half, 0, half, half)
                    TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setRegion(half, half, half, half)
                    TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setRegion(0, half, half, half)
                    else -> null
                }
                else -> null
            }
            TileContactFormType.SLOPE -> when (tileSizeType) {
                TileSizeType.HALF -> when (tileDirection) {
                    TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_WEST)
                    TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_EAST)
                    TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_EAST)
                    TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_WEST)
                    else -> null
                }
                TileSizeType.QUARTER -> when (tileOrientation) {
                    TileOrientation.HORIZONTAL -> when (tileDirection) {
                        TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setHorizontalQuarterSlopeRegion(Direction.NORTH_WEST)
                        TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setHorizontalQuarterSlopeRegion(Direction.NORTH_EAST)
                        TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setHorizontalQuarterSlopeRegion(Direction.SOUTH_EAST)
                        TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setHorizontalQuarterSlopeRegion(Direction.SOUTH_WEST)
                        else -> null
                    }
                    TileOrientation.VERTICAL -> when (tileDirection) {
                        TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setVerticalQuarterSlopeRegion(Direction.NORTH_WEST)
                        TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setVerticalQuarterSlopeRegion(Direction.NORTH_EAST)
                        TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setVerticalQuarterSlopeRegion(Direction.SOUTH_EAST)
                        TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setVerticalQuarterSlopeRegion(Direction.SOUTH_WEST)
                        else -> null
                    }
                    else -> null
                }
                TileSizeType.QUARTER_TO -> when (tileOrientation) {
                    TileOrientation.HORIZONTAL -> when (tileDirection) {
                        TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.NORTH_WEST)
                        TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.NORTH_EAST)
                        TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.SOUTH_EAST)
                        TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.SOUTH_WEST)
                        else -> null
                    }
                    TileOrientation.VERTICAL -> when (tileDirection) {
                        TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.NORTH_WEST)
                        TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.NORTH_EAST)
                        TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.SOUTH_EAST)
                        TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.SOUTH_WEST)
                        else -> null
                    }
                    else -> null
                }
                else -> null
            }
            TileContactFormType.CIRCLE -> when (tileSizeType) {
                TileSizeType.FULL -> when (dimType) {
                    TileDimType.EIGHT -> createFullCircleBitMask_8()
                    TileDimType.SIXTEEN -> createFullCircleBitMask_16()
                    else -> null
                }
                else -> null
            }
            TileContactFormType.SPIKE -> when (tileSizeType) {
                TileSizeType.FULL -> when (tileOrientation) {
                    TileOrientation.VERTICAL -> when (tileDirection) {
                            TileDirection.SOUTH -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.SOUTH_EAST)
                                .and(BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.SOUTH_WEST))
                            TileDirection.NORTH -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.NORTH_EAST)
                                .and(BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.NORTH_WEST))
                        else -> null
                    }
                    TileOrientation.HORIZONTAL -> when (tileDirection) {
                        TileDirection.WEST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.NORTH_WEST)
                            .and(BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.SOUTH_WEST))
                        TileDirection.EAST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.NORTH_EAST)
                            .and(BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.SOUTH_EAST))
                        else -> null
                    }
                    else -> null
                }
                TileSizeType.HALF -> when (tileDirection) {
                    TileDirection.SOUTH -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_EAST)
                        .and(BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_WEST))
                    TileDirection.NORTH -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_EAST)
                        .and(BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_WEST))
                    TileDirection.WEST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_WEST)
                        .and(BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_WEST))
                    TileDirection.EAST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_EAST)
                        .and(BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_EAST))
                    else -> null
                }
                else -> null
            }
            TileContactFormType.RHOMBUS -> when(tileSizeType) {
                TileSizeType.FULL -> when (dimType) {
                    TileDimType.EIGHT -> createFullRhombusBitMask_8()
                    TileDimType.SIXTEEN -> createFullRhombusBitMask_16()
                    else -> null
                }
                else -> null
            }

            else -> null
        }

        if (result != null)
            bitMaskCache[cacheKey] = result

        return result
    }

    private fun initTileSet(tileSet: TileSet) {
        // create sprite set asset for the tile set
        tileSet.spriteSetAssetId = SpriteSetAsset.build {
            name = tileSet.name
            texture(tileSet.textureAssetRef)
            tileSet.tileTemplates.forEach {
                spriteData.add(it.protoSprite)
                if (it.animationData != null)
                    spriteData.addAll(it.animationData!!.sprites.values)
            }
        }
    }

    private fun disposeTileSet(tileSet: TileSet) {
        FFContext.delete(SpriteSetAsset, tileSet.spriteSetAssetId)
        tileSet.spriteSetAssetId = NO_COMP_ID
    }
    
    override fun clearSystem() {
        systemTileSet.clear()
        bitMaskCache.clear()
    }
}