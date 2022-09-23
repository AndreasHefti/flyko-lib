package com.inari.firefly.util.geom

import com.inari.util.geom.Vector2f
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PositionFTest {

    @Test
    fun testPoint() {
        var p1 = Vector2f()

        assertTrue(p1.x == 0f)
        assertTrue(p1.y == 0f)

        p1 = Vector2f(10, 4)

        assertTrue(p1.x == 10f)
        assertTrue(p1.y == 4f)

        val p2 = Vector2f(p1)

        assertTrue(p2.x == 10f)
        assertTrue(p2.y == 4f)

    }

    @Test
    fun testToString() {
        val p1 = Vector2f(30, 40)

        assertTrue(p1.x == 30f)
        assertTrue(p1.y == 40f)

        assertEquals("[x=30.0,y=40.0]", p1.toString())
    }

    @Test
    fun testEquality() {
        val p1 = Vector2f(30, 40)
        val p2 = Vector2f(30, 40)

        assertEquals(p1, p1)
        assertEquals(p1, p2)
        assertEquals(p2, p1)
        assertEquals(p2, p2)

        assertEquals(p1, p2)
        assertEquals(p2, p1)

        val p3 = Vector2f(40, 30)

        assertNotEquals(p1, p3)
        assertNotEquals(p3, p1)
        assertNotEquals(p2, p3)
        assertNotEquals(p3, p2)
    }
}
