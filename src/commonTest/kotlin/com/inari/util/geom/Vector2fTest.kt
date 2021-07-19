package com.inari.util.geom

import kotlin.test.Test
import kotlin.test.assertEquals

class Vector2fTest {

    @Test
    fun testCreation() {
        var v = Vector2f()
        assertEquals("1.0", v.dx.toString())
        assertEquals("1.0", v.dy.toString())

        v = Vector2f(3.45f, 56.3f)
        assertEquals("3.45", v.dx.toString())
        assertEquals("56.3", v.dy.toString())
    }

}
