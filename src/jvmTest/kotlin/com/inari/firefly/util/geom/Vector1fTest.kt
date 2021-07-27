package com.inari.firefly.util.geom

import com.inari.util.geom.Vector1f
import kotlin.test.Test
import kotlin.test.assertEquals

class Vector1fTest {

    @Test
    fun testCreation() {
        var v = Vector1f()
        assertEquals("1.0", v.d.toString())

        v = Vector1f(3.45f)
        assertEquals("3.45", v.d.toString())
    }

}
