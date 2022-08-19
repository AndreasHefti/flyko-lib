package com.inari.firefly.util.collection

import com.inari.util.collection.BitSet
import org.junit.Test
import kotlin.test.assertEquals

class BitSetTest {

    @Test
    fun testInit() {
        var bitset = BitSet()
        assertEquals(1, bitset.size / 64)

        bitset = BitSet(10000)
        assertEquals(157, bitset.size / 64)
    }
}