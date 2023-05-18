package com.inari.firefly.graphics.tile

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.graphics.view.*
import com.inari.util.ZERO_FLOAT
import com.inari.util.collection.*
import com.inari.util.geom.*
import kotlin.jvm.JvmField
import kotlin.math.ceil
import kotlin.math.floor

class TileGrid private constructor(): Component(TileGrid), ViewLayerAware {

    @JvmField internal val gridDim = Vector2i(-1, -1)
    @JvmField internal val cellDim = Vector2i(-1, -1)

    override val viewIndex: ComponentIndex
        get() = viewRef.targetKey.componentIndex
    override val layerIndex: ComponentIndex
        get() = if(layerRef.targetKey.componentIndex >= 0) layerRef.targetKey.componentIndex else 0
    @JvmField val viewRef = CReference(View)
    @JvmField val layerRef = CReference(Layer)
    @JvmField var renderer: EntityRenderer = SimpleTileGridRenderer
    var gridWidth: Int
        get() = gridDim.v0
        set(value) {gridDim.v0 =  checkNotLoaded(value, "gridWidth")}
    var gridHeight: Int
        get() = gridDim.v1
        set(value) {gridDim.v1 = checkNotLoaded(value, "gridHeight")}
    var cellWidth: Int
        get() = cellDim.v0
        set(value) {cellDim.v0 = checkNotLoaded(value, "cellWidth")}
    var cellHeight: Int
        get() = cellDim.v1
        set(value) {cellDim.v1 = checkNotLoaded(value, "cellHeight")}
    @JvmField
    var position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT)
    var spherical: Boolean = false
        set(value) {field = checkNotLoaded(value, "spherical")}

    @JvmField internal var grid: Array<IntArray> = Array(0) { IntArray(0) }
    @JvmField internal val normalisedWorldBounds = Vector4i(0, 0, 0, 0)

    override fun initialize() {
        grid = Array(gridHeight) { IntArray(gridWidth) { NULL_COMPONENT_INDEX } }
        normalisedWorldBounds.width = gridWidth
        normalisedWorldBounds.height = gridHeight
        super.initialize()
    }

    operator fun get(pos: Vector2i): EntityIndex =
        if (spherical) {
            val y = pos.y % gridDim.v1
            val x = pos.x % gridDim.v0
            grid[if (y < 0) gridDim.v1 + y else y][if (x < 0) gridDim.v0 + x else x]
        } else
            checkAndGet(pos.x, pos.y)

    operator fun get(xpos: Int, ypos: Int): EntityIndex =
        if (spherical) {
            val y = ypos % gridDim.v1
            val x = xpos % gridDim.v0
            grid[if (y < 0) gridDim.v1 + y else y][if (x < 0) gridDim.v0 + x else x]
        } else
            checkAndGet(xpos, ypos)

    private fun checkAndGet(xpos: Int, ypos: Int): EntityIndex =
        if (xpos >= 0 && xpos < gridDim.v0 && ypos >= 0 && ypos < gridDim.v1)
            grid[ypos][xpos]
        else NULL_COMPONENT_INDEX

    fun getTileAt(worldPos: Vector2i): EntityIndex =
        get(
            floor((worldPos.x.toDouble() - position.x) / cellWidth).toInt(),
            floor((worldPos.y.toDouble() - position.y) / cellHeight).toInt())

    fun getTileAt(xpos: Float, ypos: Float): EntityIndex =
        get(
            floor((xpos.toDouble() - position.x) / cellWidth).toInt(),
            floor((ypos.toDouble() - position.y) / cellHeight).toInt())

    operator fun set(position: Vector2i, entityId: EntityIndex) =
        set(position.x, position.y, entityId)

    operator fun set(xpos: Int, ypos: Int, entityId: EntityIndex) =
        if (spherical) {
            val y = ypos % gridDim.v1
            val x = xpos % gridDim.v0
            grid[if (y < 0) gridDim.v1 + y else y][if (x < 0) gridDim.v0 + x else x] = entityId
        } else
            grid[ypos][xpos] = entityId

    fun reset(xpos: Int, ypos: Int): EntityIndex {
        return if (spherical) {
            val y = if (ypos % gridDim.v1 < 0) gridDim.v1 + (ypos % gridDim.v1) else ypos % gridDim.v1
            val x = if (xpos % gridDim.v0 < 0) gridDim.v0 + (xpos % gridDim.v0) else xpos % gridDim.v0
            val old = grid[y][x]
            grid[y][x] = NULL_COMPONENT_INDEX
            old
        } else {
            val old = grid[ypos][xpos]
            grid[ypos][xpos] = NULL_COMPONENT_INDEX
            old
        }
    }

    fun resetIfMatch(entityId: EntityIndex, position: Vector2i) =
        resetIfMatch(entityId, position.x, position.y)

    fun resetIfMatch(entityId: EntityIndex, xpos: Int, ypos: Int) {
        var ixpos = xpos
        var iypos = ypos
        if (spherical) {
            ixpos = if (xpos % gridDim.v0 < 0) gridDim.v0 + (xpos % gridDim.v0) else xpos % gridDim.v0
            iypos = if (ypos % gridDim.v1 < 0) gridDim.v1 + (ypos % gridDim.v1) else ypos % gridDim.v1
        }
        if (grid[iypos][ixpos] == entityId) {
            grid[iypos][ixpos] = NULL_COMPONENT_INDEX
        }
    }

    fun getNeighbour(xpos: Int, ypos: Int, direction: Direction): Int =
        getNeighbour(xpos, ypos, direction, 1, 1)

    fun getNeighbour(xpos: Int, ypos: Int, direction: Direction, xDistance: Int, yDistance: Int): Int =
        get(
            when (direction.horizontal) {
                Orientation.WEST ->  xpos - xDistance
                Orientation.EAST ->  xpos + xDistance
                else -> xpos
            },
            when (direction.vertical) {
                Orientation.NORTH -> ypos + yDistance
                Orientation.SOUTH -> ypos + yDistance
                else -> ypos
            }
        )

    val tileGridIterator: TileGridIterator
        get() = TileGrid.getTileGridIterator(this)

    fun tileGridIterator(worldClip: Vector4i): TileGridIterator =
        TileGrid.getTileGridIterator(worldClip, this)

    companion object : ComponentSystem<TileGrid>("TileGrid") {

        private val NULL_TILE_GRID: TileGrid  = TileGrid()
        private val ITERATOR_POOL = ArrayDeque<TileGridIterator>(5)
        private val VIEW_LAYER_MAPPING = ViewLayerMapping()

        private fun entityListener(key: ComponentKey, type: ComponentEventType) {
            if (type == ComponentEventType.ACTIVATED) addEntity(key.componentIndex)
            else if (type == ComponentEventType.DEACTIVATED) removeEntity(key.componentIndex)
        }

        init { Entity.registerComponentListener(this::entityListener) }

        override fun registerComponent(c: TileGrid): ComponentKey {
            val key = super.registerComponent(c)
            VIEW_LAYER_MAPPING.add(c, c.index)
            return key
        }

        override fun unregisterComponent(index: ComponentIndex) {
            val c = this[index]
            VIEW_LAYER_MAPPING.delete(c, index)
            super.unregisterComponent(index)
        }

        operator fun get(viewLayer: ViewLayerAware): BitSetRO = VIEW_LAYER_MAPPING[viewLayer]
        operator fun get(viewIndex: Int, layerIndex: Int): BitSetRO =
            VIEW_LAYER_MAPPING[viewIndex, layerIndex]

        internal fun getTileGridIterator(clip: Vector4i, tileGrid: TileGrid): TileGridIterator {
            val instance = instance

            instance.reset(clip, tileGrid)
            return instance
        }

        internal fun getTileGridIterator(tileGrid: TileGrid): TileGridIterator {
            val instance = instance

            instance.reset(tileGrid)
            return instance
        }

        private val instance: TileGridIterator
            get() {
                return if (ITERATOR_POOL.isEmpty())
                    TileGridIterator()
                else
                    ITERATOR_POOL.removeLastOrNull()!!
            }

        private fun addEntity(index: EntityIndex) {
            val entity = Entity[index]
            if (ETile !in entity.aspects)
                return
            val tile = entity[ETile]
            val tileGrid = if (tile.tileGridRef.exists)
                this[tile.tileGridRef.targetKey]
            else
                this[this[entity[ETransform]].nextSetBit(0)]

            if (EMultiplier in entity.aspects) {
                val multiplier = entity[EMultiplier]
                val pi = multiplier.positions.iterator()
                while (pi.hasNext()) {
                    val x: Int = pi.next().toInt()
                    val y: Int = pi.next().toInt()
                    tileGrid[x, y] = entity.index
                }
            } else {
                tileGrid[tile.position] = entity.index
            }
        }

        private fun removeEntity(index: EntityIndex) {
            val entity = Entity[index]
            if (ETile !in entity.aspects)
                return
            val tile = entity[ETile]
            val tileGrid = if (tile.tileGridRef.exists)
                if (this.exists(tile.tileGridRef.refIndex))
                    this[tile.tileGridRef.targetKey]
                else return
            else {
                val tileGridIndex = this[entity[ETransform]].nextSetBit(0)
                if (tileGridIndex >= 0)
                    this[tileGridIndex]
                else return
            }

            if (EMultiplier.components.contains(entity.index)) {
                val multiplier = entity[EMultiplier]
                val pi = multiplier.positions.iterator()
                while (pi.hasNext()) {
                    val x: Int = pi.next().toInt()
                    val y: Int = pi.next().toInt()
                    tileGrid.resetIfMatch(entity.index, x, y)
                }
            } else {
                tileGrid.resetIfMatch(entity.index, tile.position)
            }
        }

        override fun allocateArray(size: Int): Array<TileGrid?> = arrayOfNulls(size)
        override fun create() = TileGrid()
    }

    class TileGridIterator internal constructor() : IntIterator() {

        @JvmField internal val tmpClip = Vector4i()
        @JvmField internal val worldPosition = Vector2f()
        @JvmField internal val clip = Vector4i()
        @JvmField internal var xorig: Int = 0
        @JvmField internal var xsize: Int = 0
        @JvmField internal var ysize: Int = 0
        @JvmField internal var tileGrid: TileGrid = NULL_TILE_GRID
        @JvmField internal var hasNext: Boolean = false

        override fun hasNext(): Boolean = hasNext
        override fun nextInt(): Int {
            val result = tileGrid[clip]
            calcWorldPosition()
            clip.x++
            findNext()
            return result
        }

        internal fun reset(tileGrid: TileGrid) {
            clip(0, 0, tileGrid.gridDim.v0, tileGrid.gridDim.v1)
            init(tileGrid)
        }

        internal fun reset(clip: Vector4i, tileGrid: TileGrid) {
            mapWorldClipToTileGridClip(clip, tileGrid, this.clip)
            init(tileGrid)
        }

        private fun init(tileGrid: TileGrid) {
            xorig = clip.x
            xsize = clip.x + clip.width
            ysize = clip.y + clip.height

            this.tileGrid = tileGrid

            findNext()
        }

        private fun mapWorldClipToTileGridClip(worldClip: Vector4i, tileGrid: TileGrid, result: Vector4i) {
            tmpClip(
                floor((worldClip.x - tileGrid.position.x) / tileGrid.cellDim.v0).toInt(),
                floor((worldClip.y - tileGrid.position.y) / tileGrid.cellDim.v1).toInt()
            )
            val x2 =
                ceil((worldClip.x - tileGrid.position.x + worldClip.width) / tileGrid.cellDim.v0).toInt()
            val y2 =
                ceil((worldClip.y - tileGrid.position.y + worldClip.height) / tileGrid.cellDim.v1).toInt()
            tmpClip.width = x2 - tmpClip.x
            tmpClip.height = y2 - tmpClip.y
            GeomUtils.intersection(tmpClip, tileGrid.normalisedWorldBounds, result)
        }

        @Suppress("NOTHING_TO_INLINE")
        private fun findNext() {
            while (clip.y < ysize) {
                while (clip.x < xsize) {
                    if (tileGrid[clip] != NULL_COMPONENT_INDEX) {
                        hasNext = true
                        return
                    }
                    clip.x++
                }
                clip.x = xorig
                clip.y++
            }
            dispose()
        }

        private fun dispose() {
            hasNext = false
            tileGrid = NULL_TILE_GRID
            xorig = -1
            xsize = -1
            ysize = -1
            ITERATOR_POOL.add(this)
        }

        private fun calcWorldPosition() {
            worldPosition(
                clip.x * tileGrid.cellDim.v0,
                clip.y * tileGrid.cellDim.v1
            )
        }
    }
}