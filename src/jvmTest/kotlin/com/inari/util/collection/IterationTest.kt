package com.inari.util.collection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class IterationTest {

    @Test
    fun testDynIntArrayIteration() {
        val intArray = DynIntArray(10, -1)
        var iter = intArray.iterator()
        assertFalse(iter.hasNext())

        intArray[1] = 2
        intArray[2] = 3
        intArray[3] = 4
        intArray[4] = 5
        intArray[5] = 6
        intArray[6] = 7

        iter = intArray.iterator()
        assertEquals("2", iter.next().toString())
        assertEquals("3", iter.next().toString())
        assertEquals("4", iter.next().toString())
        assertEquals("5", iter.next().toString())
        assertEquals("6", iter.next().toString())
        assertEquals("7", iter.next().toString())
    }
}