package com.inari.util.aspect

import com.inari.util.collection.BitSet
import com.inari.util.collection.DynIntArray
import com.inari.util.indexed.Indexed

class Aspects internal constructor(
    val type: AspectType
) {

    internal val bitSet: BitSet = BitSet()
    private val tempBitSet: BitSet = BitSet()

    private constructor(source: Aspects) : this(source.type) {
        bitSet.or(source.bitSet)
    }

    val copy: Aspects
        get() = Aspects(this)

    val values: DynIntArray
        get() {
            val result = DynIntArray(bitSet.cardinality)
            var i = bitSet.nextSetBit(0)
            while (i >= 0) {
                result.add(i)
                i = bitSet.nextSetBit(i + 1)
            }
            return result
        }

    val valid: Boolean get() = !bitSet.isEmpty
    val size: Int get() = bitSet.size
    val isEmpty: Boolean get() = bitSet.cardinality <= 0

    operator fun plus(aspect: Aspect): Aspects {
        checkType(aspect)
        bitSet.set(aspect.aspectIndex)
        return this
    }

    operator fun plus(indexed: Indexed): Aspects {
        checkType(indexed)
        bitSet.set(indexed.index)
        return this
    }

    operator fun plus(aspects: Aspects): Aspects {
        checkType(aspects)
        clear()
        bitSet.or(aspects.bitSet)
        return this
    }

    operator fun plus(aspectName: String): Aspects {
        type.createAspect(aspectName)
        return this
    }

    operator fun minus(aspect: Aspect?): Aspects {
        if (aspect == null) return this
        checkType(aspect)
        bitSet[aspect.aspectIndex] = false
        return this
    }

    operator fun minus(aspects: Aspects): Aspects {
        checkType(aspects)
        bitSet.andNot(aspects.bitSet)
        return this
    }

    operator fun set(aspect: Aspect, value: Boolean) {
        checkType(aspect)
        bitSet[aspect.aspectIndex] = value
    }

    fun include(aspects: Aspects): Boolean {
        checkType(aspects)
        if (bitSet.isEmpty || aspects.bitSet.isEmpty)
            return false
        if (this == aspects)
            return true

        tempBitSet.clear()
        tempBitSet.or(bitSet)
        tempBitSet.and(aspects.bitSet)
        return tempBitSet == aspects.bitSet
    }

    fun exclude(aspects: Aspects): Boolean {
        checkType(aspects)
        if (bitSet.isEmpty || aspects.bitSet.isEmpty)
            return true
        if (this == aspects)
            return false

        tempBitSet.clear()
        tempBitSet.or(bitSet)
        tempBitSet.and(aspects.bitSet)
        return tempBitSet.isEmpty
    }

    fun intersects(aspects: Aspects): Boolean {
        checkType(aspects)
        return !exclude(aspects)
    }

    operator fun contains(aspect: Aspect?): Boolean {
        if (aspect == null) return false
        checkType(aspect)
        return bitSet[aspect.aspectIndex]
    }

    fun iterator(): Iterator<Aspect> =
        AspectIterator()

    fun clear() {
        bitSet.clear()
        tempBitSet.clear()
    }

    private fun checkType(aspects: Aspects) {
        if (aspects.type !== type)
            throw IllegalArgumentException("Aspect aspectGroup mismatch: ${aspects.type} $type")
    }

    private fun checkType(aspect: Aspect) {
        if (aspect.aspectType !== type)
            throw IllegalArgumentException("Aspect subType mismatch: " + aspect.aspectType + " " + type)
    }

    private fun checkType(indexed: Indexed) {
        if (indexed.indexedTypeName !== type.name)
            throw IllegalArgumentException("Aspect subType mismatch: " + indexed.indexedTypeName + " " + type.name)
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("Aspects [aspectType=")
        builder.append(type)
        builder.append(" {")
        val iterator = AspectIterator()
        while (iterator.hasNext()) {
            builder.append(iterator.next().aspectName)
            if (iterator.hasNext()) {
                builder.append(", ")
            }
        }
        builder.append("}")
        builder.append("]")
        return builder.toString()
    }

    private inner class AspectIterator : Iterator<Aspect> {

        private var nextSetBit = bitSet.nextSetBit(0)

        override fun hasNext(): Boolean = nextSetBit >= 0

        override fun next(): Aspect {
            val aspect = type[nextSetBit]!!
            nextSetBit = bitSet.nextSetBit(nextSetBit + 1)
            return aspect
        }
    }
}