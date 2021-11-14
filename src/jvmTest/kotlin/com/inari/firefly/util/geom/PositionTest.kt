package com.inari.firefly.util.geom

import com.inari.util.geom.Vector2i
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PositionTest {

    @Test
    fun testPoint() {
        var p1 = Vector2i()

        assertTrue(p1.x == 0)
        assertTrue(p1.y == 0)

        p1 = Vector2i(10, 4)

        assertTrue(p1.x == 10)
        assertTrue(p1.y == 4)

        val p2 = Vector2i(p1)

        assertTrue(p2.x == 10)
        assertTrue(p2.y == 4)

    }

    @Test
    fun testToString() {
        val p1 = Vector2i(30, 40)

        assertTrue(p1.x == 30)
        assertTrue(p1.y == 40)

        assertEquals("[x=30,y=40]", p1.toString())
    }

    @Test
    fun testEquality() {
        val p1 = Vector2i(30, 40)
        val p2 = Vector2i(30, 40)

        assertEquals(p1, p1)
        assertEquals(p1, p2)
        assertEquals(p2, p1)
        assertEquals(p2, p2)

        assertEquals(p1, p2)
        assertEquals(p2, p1)

        val p3 = Vector2i(40, 30)

        assertNotEquals(p1, p3)
        assertNotEquals(p3, p1)
        assertNotEquals(p2, p3)
        assertNotEquals(p3, p2)
    }

}
