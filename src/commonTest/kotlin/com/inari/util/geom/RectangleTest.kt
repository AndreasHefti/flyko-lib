package com.inari.util.geom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RectangleTest {

    @Test
    fun testRectangle() {
        var r1 = Rectangle()

        assertEquals(r1.pos.x.toLong(), 0)
        assertEquals(r1.pos.y.toLong(), 0)
        assertEquals(r1.width.toLong(), 0)
        assertEquals(r1.height.toLong(), 0)

        r1 = Rectangle(10, 10, 100, 200)

        assertEquals(r1.pos.x.toLong(), 10)
        assertEquals(r1.pos.y.toLong(), 10)
        assertEquals(r1.width.toLong(), 100)
        assertEquals(r1.height.toLong(), 200)

        val r2 = Rectangle(r1)

        assertEquals(r2.pos.x.toLong(), 10)
        assertEquals(r2.pos.y.toLong(), 10)
        assertEquals(r2.width.toLong(), 100)
        assertEquals(r2.height.toLong(), 200)

    }

    @Test
    fun testToString() {
        val r1 = Rectangle(30, 20, 111, 444)

        assertEquals(r1.pos.x.toLong(), 30)
        assertEquals(r1.pos.y.toLong(), 20)
        assertEquals(r1.width.toLong(), 111)
        assertEquals(r1.height.toLong(), 444)

        assertEquals("[x=30,y=20,width=111,height=444]", r1.toString())
    }

    fun testEquality() {
        val r1 = Rectangle(1, 1, 111, 111)
        val r2 = Rectangle(1, 1, 111, 111)

        assertEquals(r1, r2)
        assertEquals(r2, r1)

        val r3 = Rectangle(2, 1, 111, 111)

        assertNotEquals(r1, r3)
        assertNotEquals(r3, r1)
        assertNotEquals(r2, r3)
        assertNotEquals(r3, r2)
    }
}
