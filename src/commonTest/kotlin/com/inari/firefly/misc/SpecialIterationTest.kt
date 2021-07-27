package com.inari.firefly.misc

import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import kotlin.test.Test


class SpecialIterationTest {

    @Test
    fun testDynArrayIterationWithNull() {
        val array: DynArray<String> = DynArray.of()
        array[0] = "zero"
        array[3] = "three"
        array[5] = "fife"
        array[10] = "then"

        var i = 0
        while (i < array.capacity) {
            val s = array[i++] ?: continue
            println(s)
        }
    }

    @Test
    fun testBitSet() {
        val bitSet: BitSet = BitSet()
        bitSet.set(0)
        bitSet.set(3)
        bitSet.set(5)
        bitSet.set(10)
        bitSet.set(100)
        bitSet.set(1000)
        bitSet.set(10000)
        bitSet.set(100000)


        var i = bitSet.nextSetBit(0)
        while (i >= 0) {
            println(i)
            i = bitSet.nextSetBit(i + 1)
        }

        val iterator = BitSetIterator(bitSet)
        while(iterator.hasNext()) {
            println(iterator.next())
        }

    }

    private class BitSetIterator(val set: BitSet) : IntIterator() {

        private var index = set.nextSetBit(0)

        override fun nextInt(): Int {
            val result = index
            index = set.nextSetBit(index + 1)
            return result
        }

        override fun hasNext(): Boolean =
            index >= 0
    }
}