package com.inari.util.collection

import com.inari.util.IntIterator


class BitSetIterator(val bitset: BitSet) : IntIterator {

    private var index = bitset.nextSetBit(0)
    override fun hasNext(): Boolean = index >= 0
    override fun next(): Int {
        val ret = index
        index = bitset.nextSetBit(index + 1)
        return ret
    }
}