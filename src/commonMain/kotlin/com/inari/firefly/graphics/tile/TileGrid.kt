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

    val view = ComponentRefResolver(View) { index-> viewRef = index }
    val layer = ComponentRefResolver(Layer) { index-> layerRef = index }
    var renderer = ComponentRefResolver(Renderer) { index-> rendererRef = index }
    var gridWidth: Int
        get() = gridDim.dx
        set(value) {gridDim.dx = setIfNotInitialized(value, "gridWidth")}
    var gridHeight: Int
        get() = gridDim.dy
        set(value) {gridDim.dy = setIfNotInitialized(value, "gridHeight")}
    var cellWidth: Int
        get() = cellDim.dx
        set(value) {cellDim.dx = setIfNotInitialized(value, "cellWidth")}
    var cellHeight: Int
        get() = cellDim.dy
        set(value) {cellDim.dy = setIfNotInitialized(value, "cellHeight")}
    var position: PositionF = PositionF(ZERO_FLOAT, ZERO_FLOAT)
    var spherical: Boolean = false
        set(value) {field = setIfNotInitialized(value, "spherical")}

    override val viewIndex: Int
        get() = viewRef
    override val layerIndex: Int
        get() = layerRef

    @JvmField internal var grid: Array<IntArray> = Array(0) { IntArray(0) }
    @JvmField internal val normalisedWorldBounds = Rectangle(0, 0, 0, 0)

    override fun init() {
        grid = Array(gridHeight) { IntArray(gridWidth) { -1 } }
        normalisedWorldBounds.width = gridWidth
        normalisedWorldBounds.height = gridHeight

        super.init()
    }

    operator fun get(rectPos: Rectangle): Int =
        this[rectPos.pos]

    operator fun get(pos: Position): Int =
        if (spherical) {
            val y = pos.y % gridDim.dy
            val x = pos.x % gridDim.dx
            grid[if (y < 0) gridDim.dy + y else y][if (x < 0) gridDim.dx + x else x]
        } else
            checkAndGet(pos.x, pos.y)

    operator fun get(xpos: Int, ypos: Int): Int =
        if (spherical) {
            val y = ypos % gridDim.dy
            val x = xpos % gridDim.dx
            grid[if (y < 0) gridDim.dy + y else y][if (x < 0) gridDim.dx + x else x]
        } else
            checkAndGet(xpos, ypos)

    private fun checkAndGet(xpos: Int, ypos: Int): Int =
        if (xpos >= 0 && xpos < gridDim.dx && ypos >= 0 && ypos < gridDim.dy)
            grid[ypos][xpos]
        else -1

    fun getTileAt(worldPos: Position): Int =
        get(floor((worldPos.x.toDouble() - position.x) / cellWidth).toInt(),
            floor((worldPos.y.toDouble() - position.y) / cellHeight).toInt())

    fun getTileAt(xpos: Float, ypos: Float): Int =
        get(floor((xpos.toDouble() - position.x) / cellWidth).toInt(),
            floor((ypos.toDouble() - position.y) / cellHeight).toInt())

    operator fun set(position: Position, entityId: Int) =
        set(position.x, position.y, entityId)

    operator fun set(xpos: Int, ypos: Int, entityId: Int) =
        if (spherical) {
            val y = ypos % gridDim.dy
            val x = xpos % gridDim.dx
            grid[if (y < 0) gridDim.dy + y else y][if (x < 0) gridDim.dx + x else x] = entityId
        } else
            grid[ypos][xpos] = entityId


    fun reset(xpos: Int, ypos: Int): Int {
        return if (spherical) {
            val y = if (ypos % gridDim.dy < 0) gridDim.dy + (ypos % gridDim.dy) else ypos % gridDim.dy
            val x = if (xpos % gridDim.dx < 0) gridDim.dx + (xpos % gridDim.dx) else xpos % gridDim.dx
            val old = grid[y][x]
            grid[y][x] = -1
            old
        } else {
            val old = grid[ypos][xpos]
            grid[ypos][xpos] = -1
            old
        }
    }

    fun resetIfMatch(entityId: Int, position: Position) =
        resetIfMatch(entityId, position.x, position.y)

    fun resetIfMatch(entityId: Int, xpos: Int, ypos: Int) {
        var ixpos = xpos
        var iypos = ypos
        if (spherical) {
            ixpos = if (xpos % gridDim.dx < 0) gridDim.dx + (xpos % gridDim.dx) else xpos % gridDim.dx
            iypos = if (ypos % gridDim.dy < 0) gridDim.dy + (ypos % gridDim.dy) else ypos % gridDim.dy
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

    fun tileGridIterator(worldClip: Rectangle): TileGridIterator =
        TileGridIterator.getInstance(worldClip, this)

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<TileGrid>(TileGrid::class) {
        override fun createEmpty() = TileGrid()
    }


    class TileGridIterator private constructor() : IntIterator() {

        @JvmField internal val tmpClip = Rectangle()
        @JvmField internal val worldPosition = PositionF()
        @JvmField internal val clip = Rectangle()
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
            clip(0, 0, tileGrid.gridDim.dx, tileGrid.gridDim.dy)
            init(tileGrid)
        }

        private fun reset(clip: Rectangle, tileGrid: TileGrid) {
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

        private fun mapWorldClipToTileGridClip(worldClip: Rectangle, tileGrid: TileGrid, result: Rectangle) {
            tmpClip(
                floor((worldClip.x.toDouble() - tileGrid.position.x) / tileGrid.cellDim.dx).toInt(),
                floor((worldClip.y.toDouble() - tileGrid.position.y) / tileGrid.cellDim.dy).toInt()
            )
            val x2 = ceil((worldClip.x.toDouble() - tileGrid.position.x + worldClip.width) / tileGrid.cellDim.dx).toInt()
            val y2 = ceil((worldClip.y.toDouble() - tileGrid.position.y + worldClip.height) / tileGrid.cellDim.dy).toInt()
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
                tileGrid.position.x + clip.x * tileGrid.cellDim.dx,
                tileGrid.position.y + clip.y * tileGrid.cellDim.dy
            )
        }

        companion object {

            private val NULL_TILE_GRID: TileGrid  = TileGrid()
            private val POOL = ArrayDeque<TileGridIterator>(5)

            internal fun getInstance(clip: Rectangle, tileGrid: TileGrid): TileGridIterator {
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