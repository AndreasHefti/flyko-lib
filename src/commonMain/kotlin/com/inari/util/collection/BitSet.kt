package com.inari.util.collection

import kotlin.math.min

interface BitSetRO {
    val cardinality: Int
    val isEmpty: Boolean
    fun length(): Int
    val size: Int
    operator fun get(pos: Int): Boolean
    operator fun get(from: Int, to: Int): BitSet?
    fun intersects(set: BitSet): Boolean
    fun nextClearBit(from: Int): Int
    fun nextSetBit(from: Int): Int
    fun containsAll(other: BitSet): Boolean
}

class BitSet(nBits: Int = 64) : BitSetRO {

    private var bits: LongArray

    init {
        var length:Int = nBits ushr 6
        if ((nBits and LONG_MASK) != 0)
            ++length
        bits = LongArray(length)
    }

    fun and(bs: BitSet) {
        val max: Int = min(bits.size, bs.bits.size)
        var i = 0
        while (i < max) {
            bits[i] = bits[i] and bs.bits[i]
            ++i
        }
        while (i < bits.size) bits[i++] = 0
    }

    fun andNot(bs: BitSet) {
        var i: Int = min(bits.size, bs.bits.size)
        while (--i >= 0) bits[i] = bits[i] and bs.bits[i].inv()
    }

    override val cardinality: Int
        get() {
            var result = 0
            for (i in bits.size - 1 downTo 0) {
                var a = bits[i]
                // Common cases.
                if (a == 0L) continue
                if (a == -1L) {
                    result += 64
                    continue
                }

                // Successively collapse alternating bit groups into a sum.
                a = (a shr 1 and 0x5555555555555555L) + (a and 0x5555555555555555L)
                a = (a shr 2 and 0x3333333333333333L) + (a and 0x3333333333333333L)
                var b = ((a ushr 32) + a).toInt()
                b = (b shr 4 and 0x0f0f0f0f) + (b and 0x0f0f0f0f)
                b = (b shr 8 and 0x00ff00ff) + (b and 0x00ff00ff)
                result += (b shr 16 and 0x0000ffff) + (b and 0x0000ffff)
            }
            return result
        }

    fun clear() {
        bits.fill(0)
    }

    fun clear(pos: Int) {
        val offset = pos shr 6
        ensure(offset)
        bits[offset] = bits[offset] and (1L shl pos).inv()
    }

    fun clear(from: Int, to: Int) {
        if (from < 0 || from > to) throw IndexOutOfBoundsException()
        if (from == to) return
        val lo_offset = from ushr 6
        val hi_offset = to ushr 6
        ensure(hi_offset)
        if (lo_offset == hi_offset) {
            bits[hi_offset] = bits[hi_offset] and ((1L shl from) - 1 or (-1L shl to))
            return
        }
        bits[lo_offset] = bits[lo_offset] and (1L shl from) - 1
        bits[hi_offset] = bits[hi_offset] and (-1L shl to)
        for (i in lo_offset + 1 until hi_offset) bits[i] = 0
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BitSet) return false
        val max: Int = min(bits.size, other.bits.size)
        var i: Int = 0
        while (i < max) {
            if (bits[i] != other.bits[i]) return false
            ++i
        }
        // If one is larger, check to make sure all extra bits are 0.
        for (j in i until bits.size) if (bits[j] != 0L) return false
        for (j in i until other.bits.size) if (other.bits[j] != 0L) return false
        return true
    }

    fun flip(index: Int) {
        val offset = index shr 6
        ensure(offset)
        bits[offset] = bits[offset] xor (1L shl index)
    }

    fun flip(from: Int, to: Int) {
        if (from < 0 || from > to) throw IndexOutOfBoundsException()
        if (from == to) return
        val lo_offset = from ushr 6
        val hi_offset = to ushr 6
        ensure(hi_offset)
        if (lo_offset == hi_offset) {
            bits[hi_offset] = bits[hi_offset] xor (-1L shl from and (1L shl to) - 1)
            return
        }
        bits[lo_offset] = bits[lo_offset] xor (-1L shl from)
        bits[hi_offset] = bits[hi_offset] xor (1L shl to) - 1
        for (i in lo_offset + 1 until hi_offset) bits[i] = bits[i] xor -1
    }

    override operator fun get(pos: Int): Boolean {
        val offset = pos shr 6
        return if (offset >= bits.size) false else bits[offset] and (1L shl pos) != 0L
    }

    override operator fun get(from: Int, to: Int): BitSet? {
        if (from < 0 || from > to) throw IndexOutOfBoundsException()
        val bs = BitSet(to - from)
        var lo_offset = from ushr 6
        if (lo_offset >= bits.size || to == from) return bs
        val lo_bit = from and LONG_MASK
        val hi_offset = to ushr 6
        if (lo_bit == 0) {
            val len = min(hi_offset - lo_offset + 1, bits.size - lo_offset)
            bits.copyInto(bs.bits, 0, lo_offset, lo_offset + len)
            //System.arraycopy(bits, lo_offset, bs.bits, 0, len)
            if (hi_offset < bits.size) bs.bits[hi_offset - lo_offset] =
                bs.bits[hi_offset - lo_offset] and (1L shl to) - 1
            return bs
        }
        val len = min(hi_offset, bits.size - 1)
        val reverse = 64 - lo_bit
        var i: Int = 0
        while (lo_offset < len) {
            bs.bits[i] = (bits[lo_offset] ushr lo_bit
                    or (bits[lo_offset + 1] shl reverse))
            lo_offset++
            i++
        }
        if (to and LONG_MASK > lo_bit) bs.bits[i++] = bits[lo_offset] ushr lo_bit
        if (hi_offset < bits.size) bs.bits[i - 1] = bs.bits[i - 1] and (1L shl to - from) - 1
        return bs
    }

    override fun hashCode(): Int {
        var h: Long = 1234
        var i: Int = bits.size
        while (i > 0) {
            h = h xor i * bits[--i]
        }
        return (h shr 32 xor h).toInt()
    }

    override fun intersects(set: BitSet): Boolean {
        var i: Int = min(bits.size, set.bits.size)
        while (--i >= 0) if (bits[i] and set.bits[i] != 0L) return true
        return false
    }

    override val isEmpty: Boolean
        get() {
            for (i in bits.size - 1 downTo 0) if (bits[i] != 0L) return false
            return true
        }

    override fun length(): Int {
        // Set i to highest index that contains a non-zero value.
        var i: Int = bits.size - 1
        while (i >= 0 && bits[i] == 0L) {
            --i
        }

        // if i < 0 all bits are cleared.
        if (i < 0) return 0

        // Now determine the exact length.
        var b = bits[i]
        var len = (i + 1) * 64
        // b >= 0 checks if the highest bit is zero.
        while (b >= 0) {
            --len
            b = b shl 1
        }
        return len
    }

    override fun nextClearBit(from: Int): Int {
        var _from = from
        var offset = _from shr 6
        var mask = 1L shl _from
        while (offset < bits.size) {
            val h = bits[offset]
            do {
                if (h and mask == 0L) return _from
                mask = mask shl 1
                _from++
            } while (mask != 0L)
            mask = 1
            offset++
        }
        return _from
    }

    override fun nextSetBit(from: Int): Int {
        var _from = from
        var offset = _from shr 6
        var mask = 1L shl _from
        while (offset < bits.size) {
            val h = bits[offset]
            do {
                if (h and mask != 0L) return _from
                mask = mask shl 1
                _from++
            } while (mask != 0L)
            mask = 1
            offset++
        }
        return -1
    }

    fun or(bs: BitSet) {
        ensure(bs.bits.size - 1)
        for (i in bs.bits.size - 1 downTo 0) bits[i] = bits[i] or bs.bits[i]
    }

    fun set(pos: Int) {
        val offset = pos shr 6
        ensure(offset)
        bits[offset] = bits[offset] or (1L shl pos)
    }

    operator fun set(index: Int, value: Boolean) {
        if (value) set(index) else clear(index)
    }

    fun set(from: Int, to: Int) {
        if (from < 0 || from > to) throw IndexOutOfBoundsException()
        if (from == to) return
        val lo_offset = from ushr 6
        val hi_offset = to ushr 6
        ensure(hi_offset)
        if (lo_offset == hi_offset) {
            bits[hi_offset] = bits[hi_offset] or (-1L shl from and (1L shl to) - 1)
            return
        }
        bits[lo_offset] = bits[lo_offset] or (-1L shl from)
        bits[hi_offset] = bits[hi_offset] or (1L shl to) - 1
        for (i in lo_offset + 1 until hi_offset) bits[i] = -1
    }

    fun set(from: Int, to: Int, value: Boolean) {
        if (value) set(from, to) else clear(from, to)
    }

    override val size: Int
        get() = bits.size * 64

    fun xor(bs: BitSet) {
        ensure(bs.bits.size - 1)
        for (i in bs.bits.size - 1 downTo 0) bits[i] = bits[i] xor bs.bits[i]
    }

    private fun ensure(lastElt: Int) {
        if (lastElt >= bits.size) {
            bits = bits.copyOf(lastElt + 1)
        }
    }

    override fun containsAll(other: BitSet): Boolean {
        for (i in other.bits.size - 1 downTo 0) {
            if (bits[i] and other.bits[i] != other.bits[i]) return false
        }
        return true
    }


    companion object {
        private const val LONG_MASK = 0x3f
    }
}