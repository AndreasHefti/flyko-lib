package com.inari.util.collection

import com.inari.util.arraycopy

interface DynIntArrayRO : IndexIterable {
    val nullValue: Int
    val expand: Int
    val isEmpty: Boolean
    val size: Int
    val length: Int
    fun isEmpty(index: Int): Boolean
    operator fun contains(value: Int): Boolean
    operator fun get(index: Int): Int
    fun indexOf(value: Int): Int
    operator fun iterator(): IntIterator
}

class DynIntArray(
    initSize: Int = 10,
    override val nullValue: Int = Int.MIN_VALUE,
    override val expand: Int = 10
) : DynIntArrayRO {

    private var array: IntArray = IntArray(initSize) { nullValue }
    override var size = 0
        private set
    override val isEmpty: Boolean
        get() = size <= 0
    override val length: Int
        get() = array.size

    constructor(bits: BitSet, nullValue: Int, expand: Int) : this(bits.cardinality, nullValue, expand) {
        initArray(bits.cardinality)
        var i = bits.nextSetBit(0)
        while (i >= 0) {
            add(i)
            i = bits.nextSetBit(i + 1)
        }
    }

    fun clear() {
        var i = 0
        while (i < array.size) {
            array[i] = nullValue
            i++
        }
        size = 0
    }

    operator fun set(index: Int, value: Int) {
        if (value == nullValue)
            return
        ensureCapacity(index)
        if (array[index] == nullValue)
            size++
        array[index] = value
    }

    operator fun plus(value: Int) = add(value)

    fun add(value: Int): Int {
        if (value == nullValue)
            return -1
        val firstEmptyIndex = firstEmptyIndex()
        if (firstEmptyIndex >= 0) {
            array[firstEmptyIndex] = value
            size++
            return firstEmptyIndex
        }

        val oldLength = array.size
        expand(0)
        array[oldLength] = value
        size++
        return oldLength
    }

    operator fun plus(toAdd: DynIntArrayRO) = addAll(toAdd)

    fun addAll(toAdd: DynIntArrayRO) {
        for (i in 0 until toAdd.length) {
            if (toAdd.isEmpty(i))
                continue
            add(toAdd[i])
        }
    }

    fun addAll(vararg values: Int) {
        for (value in values)
            add(value)
    }

    operator fun plus(intIterator: IntIterator?) = addAll(intIterator)

    fun addAll(intIterator: IntIterator?) {
        if (intIterator == null)
            return
        while (intIterator.hasNext())
            add(intIterator.next())
    }

    override fun isEmpty(index: Int): Boolean =
        array[index] == nullValue

    override fun contains(value: Int): Boolean {
        var i = 0
        while (i < array.size) {
            if (array[i] == value)
                return true
            i++
        }
        return false
    }

    operator fun minus(value: Int) = remove(value)

    fun remove(value: Int): Boolean {
        val indexOf = indexOf(value)
        if (indexOf >= 0) {
            array[indexOf] = nullValue
            size--
            return true
        }

        return false
    }

    fun removeAt(index: Int): Int {
        val result = array[index]
        array[index] = nullValue
        return result
    }

    fun swap(index1: Int, index2: Int) {
        val tmp = array[index1]
        array[index1] = array[index2]
        array[index2] = tmp
    }

    override fun get(index: Int): Int =
        array[index]

    override fun iterator(): IntIterator =
        IndexIterator(this)

    override fun nextIndex(from: Int): Int {
        var currentIndex = from
        while (currentIndex < array.size && array[currentIndex] == nullValue)
            currentIndex++
        if (currentIndex < array.size)
            return currentIndex
        return -1
    }

    private fun firstEmptyIndex(): Int =
        indexOf(nullValue)

    override fun indexOf(value: Int): Int {
        var i = 0
        while (i < array.size) {
            if (array[i] == value)
                return i
            i++
        }

        return -1
    }

    /** Sorts the list within the given comparator.
     * @param comparator
     */
    fun sort(comparator: Comparator<Int>) {
        trim_all()
        array.sortedWith(comparator)
    }

    fun trim_head_tail() {
        var startIndex = 0
        var endIndex = array.size -1
        while (array[startIndex] == nullValue || startIndex >= array.size) {
            startIndex++
        }
        while (array[endIndex] == nullValue || endIndex == 0) {
            endIndex--
        }
        val newSize = endIndex + 1 - startIndex
        if (newSize <= 0)
            throw IllegalStateException()

        val temp = array
        initArray(newSize)
        arraycopy(temp, startIndex, array, 0, newSize)
    }

    fun trim_all() {
        var i = 0
        while (i < array.size) {
            if (array[i] == nullValue) {
                for (j in array.size - 1 downTo i + 1) {
                    if (array[j] != nullValue) {
                        array[i] = array[j]
                        array[j] = nullValue
                        break
                    }
                }
            }
            i++
        }

        if (array.size != size) {
            val temp = array
            initArray(size)
            arraycopy(temp, 0, array, 0, size)
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("IntBag [nullValue=").append(nullValue)
            .append(", expand=").append(expand).append(", size=").append(size)
            .append(", length=").append(length)
            .append(", array=").append(array.contentToString()).append("]")
        return builder.toString()
    }

    private fun ensureCapacity(size: Int) {
        if (array.size <= size) {
            expand(size - array.size + 1)
        }
    }

    private fun initArray(size: Int) {
        array = IntArray(size)
        var i = 0
        while (i < array.size) {
            array[i] = nullValue
            i++
        }
    }

    private fun expand(expandSize: Int) {
        val temp = array
        initArray(temp.size + expandSize + expand)
        arraycopy(temp, 0, array, 0, temp.size)
    }
}