package com.inari.firefly.util.geom

import com.inari.util.geom.Vector2f
import com.inari.util.timeMillis
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

    @Test
    fun testAssign() {

        var v1 = Vector2f(1f, 1f)
        var v2 = Vector2f(1f, 1f)
        val time = timeMillis()
        for (i in 0..10000)
            v1 + v2
        println( "+++++" + (timeMillis() - time))
        assertEquals("[dx=10002.0,dy=10002.0]", v1.toString())

    }

}
