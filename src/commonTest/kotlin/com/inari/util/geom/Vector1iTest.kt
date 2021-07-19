package com.inari.util.geom

import kotlin.test.Test
import kotlin.test.assertEquals


class Vector1iTest {

    @Test
    fun testCreation() {
        var v = Vector1i()
        assertEquals("1", v.d.toString())

        v = Vector1i(3)
        assertEquals("3", v.d.toString())
    }

}
