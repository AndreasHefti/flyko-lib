package com.inari.firefly.util.collection

import com.inari.util.collection.DynArray
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DynArrayTest {

    @Test
    fun testInit() {
        val array: DynArray<String> = DynArray.of(0,1)
        assertNotNull(array)
        assertEquals(0, array.size)
        assertEquals(0, array.capacity)
        array + "1"
        assertEquals(1, array.size)
        assertEquals(1, array.capacity)
        array + "2"
        assertEquals(2, array.size)
        assertEquals(2, array.capacity)
    }

    @Test
    fun testIterateEmptyArray() {
        val array: DynArray<String> = DynArray.of()
        array.forEach {
            println(it)
        }
    }

    @Test
    fun testNextIndex() {
        val array: DynArray<String> = DynArray.of()
        var index = array.nextIndex(0)
        assertEquals("-1", "$index")
    }
}