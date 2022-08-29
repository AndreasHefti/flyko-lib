package com.inari.firefly.graphics.tile

import com.inari.firefly.TestApp
import com.inari.firefly.graphics.view.View.Companion.BASE_VIEW_KEY
import kotlin.test.Test
import kotlin.test.assertEquals

class TileGridSystemTest {

    @Test
    fun testSystemInit() {
        TestApp
        TileGrid
    }

    @Test
    fun testSpherical() {
        TestApp

        val grid = TileGrid.buildAndGet {
            viewRef(BASE_VIEW_KEY)
            spherical = true
            gridWidth = 3
            gridHeight = 3
        }
        grid[0,0] = 11
        grid[0,1] = 12
        grid[0,2] = 13
        grid[1,0] = 21
        grid[1,1] = 22
        grid[1,2] = 23
        grid[2,0] = 31
        grid[2,1] = 32
        grid[2,2] = 33

        assertEquals(11, grid[0,0])
        assertEquals(31, grid[2,0])
        assertEquals(11, grid[3,0])

        assertEquals(31, grid[-1,0])
        assertEquals(21, grid[-2,0])
        assertEquals(11, grid[-3,0])
        assertEquals(31, grid[-4,0])
    }
}