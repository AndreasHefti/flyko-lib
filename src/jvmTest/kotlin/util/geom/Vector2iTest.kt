package com.inari.firefly.util.geom

import com.inari.util.geom.Vector2i
import kotlin.test.Test
import kotlin.test.assertEquals

class Vector2iTest {

    @Test
    fun testCreation() {
        var v = Vector2i()
        assertEquals("0", v.v0.toString())
        assertEquals("0", v.v1.toString())

        v = Vector2i(3, 56)
        assertEquals("3", v.v0.toString())
        assertEquals("56", v.v1.toString())
    }

}
