package com.inari.firefly.util.collection

import com.inari.util.collection.BitSet
import com.inari.util.collection.IndexIterator
import org.junit.Test
import kotlin.test.assertEquals

class BitSetTest {

    @Test
    fun testInit() {
        var bitset = BitSet()
        assertEquals(1, bitset.size / 64)

        bitset = BitSet(10000)
        assertEquals(157, bitset.size / 64)

        bitset = BitSet(0)
        assertEquals(0, bitset.size )

        bitset.set(3)
        assertEquals(1, bitset.size / 64)
    }

    @Test
    fun testIterator() {
        var bitset = BitSet()
        bitset[0] = true
        bitset[1] = true
        bitset[3] = true
        bitset[4] = true
        bitset[5] = true

        val iterator = IndexIterator(bitset)
        val buffer = StringBuffer()
        while (iterator.hasNext())
            buffer.append("${iterator.next()},")


        assertEquals("0,1,3,4,5,", buffer.toString())
    }
}