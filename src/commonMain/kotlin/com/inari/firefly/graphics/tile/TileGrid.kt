package com.inari.firefly.graphics.tile

import com.inari.firefly.ZERO_FLOAT
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.graphics.rendering.Renderer
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.util.geom.*
import kotlin.jvm.JvmField
import kotlin.math.ceil
import kotlin.math.floor


class TileGrid private constructor() : SystemComponent(TileGrid::class.simpleName!!), ViewLayerAware {

    @JvmField internal var viewRef = -1
    @JvmField internal var layerRef = 0
    @JvmField internal var rendererRef = -1
    @JvmField internal val gridDim = Vector2i(-1, -1)
    @JvmField internal val cellDim = Vector2i(-1, -1)

    @JvmField val view = ComponentRefResolver(View) { index-> viewRef = index }
    @JvmField val layer = ComponentRefResolver(Layer) { index-> layerRef = index }
    @JvmField var renderer = ComponentRefResolver(Renderer) { index-> rendererRef = index }
    var gridWidth: Int
        get() = gridDim.v0
        set(value) {gridDim.v0 = setIfNotInitialized(value, "gridWidth")}
    var gridHeight: Int
        get() = gridDim.v1
        set(value) {gridDim.v1 = setIfNotInitialized(value, "gridHeight")}
    var cellWidth: Int
        get() = cellDim.v0
        set(value) {cellDim.v0 = setIfNotInitialized(value, "cellWidth")}
    var cellHeight: Int
        get() = cellDim.v1
        set(value) {cellDim.v1 = setIfNotInitialized(value, "cellHeight")}
    @JvmField var position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT)
    var spherical: Boolean = false
        set(value) {field = setIfNotInitialized(value, "spherical")}

    override val viewIndex: Int
        get() = viewRef
    override val layerIndex: Int
        get() = layerRef

    @JvmField internal var grid: Array<IntArray> = Array(0) { IntArray(0) }
    @JvmField internal val normalisedWorldBounds = Vector4i(0, 0, 0, 0)

    override fun init() {
        grid = Array(gridHeight) { IntArray(gridWidth) { -1 } }
        normalisedWorldBounds.width = gridWidth
        normalisedWorldBounds.height = gridHeight

        super.init()
    }

    operator fun get(pos: Vector2i): Int =
        if (spherical) {
            val y = pos.y % gridDim.v1
            val x = pos.x % gridDim.v0
            grid[if (y < 0) gridDim.v1 + y else y][if (x < 0) gridDim.v0 + x else x]
        } else
            checkAndGet(pos.x, pos.y)

    operator fun get(xpos: Int, ypos: Int): Int =
        if (spherical) {
            val y = ypos % gridDim.v1
            val x = xpos % gridDim.v0
            grid[if (y < 0) gridDim.v1 + y else y][if (x < 0) gridDim.v0 + x else x]
        } else
            checkAndGet(xpos, ypos)

    private fun checkAndGet(xpos: Int, ypos: Int): Int =
        if (xpos >= 0 && xpos < gridDim.v0 && ypos >= 0 && ypos < gridDim.v1)
            grid[ypos][xpos]
        else -1

    fun getTileAt(worldPos: Vector2i): Int =
        get(floor((worldPos.x.toDouble() - position.x) / cellWidth).toInt(),
            floor((worldPos.y.toDouble() - position.y) / cellHeight).toInt())

    fun getTileAt(xpos: Float, ypos: Float): Int =
        get(floor((xpos.toDouble() - position.x) / cellWidth).toInt(),
            floor((ypos.toDouble() - position.y) / cellHeight).toInt())

    operator fun set(position: Vector2i, entityId: Int) =
        set(position.x, position.y, entityId)

    operator fun set(xpos: Int, ypos: Int, entityId: Int) =
        if (spherical) {
            val y = ypos % gridDim.v1
            val x = xpos % gridDim.v0
            grid[if (y < 0) gridDim.v1 + y else y][if (x < 0) gridDim.v0 + x else x] = entityId
        } else
            grid[ypos][xpos] = entityId


    fun reset(xpos: Int, ypos: Int): Int {
        return if (spherical) {
            val y = if (ypos % gridDim.v1 < 0) gridDim.v1 + (ypos % gridDim.v1) else ypos % gridDim.v1
            val x = if (xpos % gridDim.v0 < 0) gridDim.v0 + (xpos % gridDim.v0) else xpos % gridDim.v0
            val old = grid[y][x]
            grid[y][x] = -1
            old
        } else {
            val old = grid[ypos][xpos]
            grid[ypos][xpos] = -1
            old
        }
    }

    fun resetIfMatch(entityId: Int, position: Vector2i) =
        resetIfMatch(entityId, position.x, position.y)

    fun resetIfMatch(entityId: Int, xpos: Int, ypos: Int) {
        var ixpos = xpos
        var iypos = ypos
        if (spherical) {
            ixpos = if (xpos % gridDim.v0 < 0) gridDim.v0 + (xpos % gridDim.v0) else xpos % gridDim.v0
            iypos = if (ypos % gridDim.v1 < 0) gridDim.v1 + (ypos % gridDim.v1) else ypos % gridDim.v1
        }
        if (grid[iypos][ixpos] == entityId) {
            grid[iypos][ixpos] = -1
        }
    }

    fun getNeighbour(xpos: Int, ypos: Int, direction: Direction): Int {
        return getNeighbour(xpos, ypos, direction, 1, 1)
    }

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
        get() = TileGridIterator.getInstance(this)

    fun tileGridIterator(worldClip: Vector4i): TileGridIterator =
        TileGridIterator.getInstance(worldClip, this)

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<TileGrid>(TileGrid::class) {
        override fun createEmpty() = TileGrid()
    }


    class TileGridIterator private constructor() : IntIterator() {

        @JvmField internal val tmpClip = Vector4i()
        @JvmField internal val worldPosition = Vector2f()
        @JvmField internal val clip = Vector4i()
        @JvmField internal var xorig: Int = 0
        @JvmField internal var xsize: Int = 0
        @JvmField internal var ysize: Int = 0
        @JvmField internal var tileGrid: TileGrid = NULL_TILE_GRID
        @JvmField internal var hasNext: Boolean = false

        val worldXPos: Float get() = worldPosition.x
        val worldYPos: Float get() = worldPosition.y

        override fun hasNext(): Boolean = hasNext
        override fun nextInt(): Int {
            val result = tileGrid[clip]
            calcWorldPosition()
            clip.x++
            findNext()
            return result
        }

        private fun reset(tileGrid: TileGrid) {
            clip(0, 0, tileGrid.gridDim.v0, tileGrid.gridDim.v1)
            init(tileGrid)
        }

        private fun reset(clip: Vector4i, tileGrid: TileGrid) {
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
                floor((worldClip.x.toDouble() - tileGrid.position.x) / tileGrid.cellDim.v0).toInt(),
                floor((worldClip.y.toDouble() - tileGrid.position.y) / tileGrid.cellDim.v1).toInt()
            )
            val x2 = ceil((worldClip.x.toDouble() - tileGrid.position.x + worldClip.width) / tileGrid.cellDim.v0).toInt()
            val y2 = ceil((worldClip.y.toDouble() - tileGrid.position.y + worldClip.height) / tileGrid.cellDim.v1).toInt()
            tmpClip.width = x2 - tmpClip.x
            tmpClip.height = y2 - tmpClip.y
            GeomUtils.intersection(tmpClip, tileGrid.normalisedWorldBounds, result)
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun findNext() {
            while (clip.y < ysize) {
                while (clip.x < xsize) {
                    if (tileGrid[clip] != -1) {
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
            POOL.add(this)
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun calcWorldPosition() {
            worldPosition(
                tileGrid.position.x + clip.x * tileGrid.cellDim.v0,
                tileGrid.position.y + clip.y * tileGrid.cellDim.v1
            )
        }

        companion object {

            private val NULL_TILE_GRID: TileGrid  = TileGrid()
            private val POOL = ArrayDeque<TileGridIterator>(5)

            internal fun getInstance(clip: Vector4i, tileGrid: TileGrid): TileGridIterator {
                val instance = instance

                instance.reset(clip, tileGrid)
                return instance
            }

            internal fun getInstance(tileGrid: TileGrid): TileGridIterator {
                val instance = instance

                instance.reset(tileGrid)
                return instance
            }

            private val instance: TileGridIterator
                get() {
                    return if (POOL.isEmpty())
                        TileGridIterator()
                     else
                        POOL.removeLastOrNull()!!
                }
        }
    }
}