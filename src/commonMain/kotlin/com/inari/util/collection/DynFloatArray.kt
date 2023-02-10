package com.inari.util.collection

import com.inari.util.arraycopy

interface DynFloatArrayRO : IndexedTypeIterable<Float> {
    val nullValue: Float
    val expand: Int
    val isEmpty: Boolean
    val size: Int
    val length: Int
    fun isEmpty(index: Int): Boolean
    operator fun contains(value: Float): Boolean
    override operator fun get(index: Int): Float
    fun indexOf(value: Float): Int
    operator fun iterator(): IndexedTypeIterator<Float>
}

class DynFloatArray(
    initSize: Int = 10,
    override val nullValue: Float = Float.NEGATIVE_INFINITY,
    override val expand: Int = 10
) : DynFloatArrayRO {

    private var array: FloatArray = FloatArray(initSize) { nullValue }
    override var size = 0
        private set
    override val isEmpty: Boolean
        get() = size <= 0
    override val length: Int
        get() = array.size

    fun clear() {
        var i = 0
        while (i < array.size) {
            array[i] = nullValue
            i++
        }
        size = 0
    }

    operator fun set(index: Int, value: Float) {
        if (value == nullValue)
            return
        ensureCapacity(index)
        if (array[index] == nullValue)
            size++
        array[index] = value
    }

    fun add(value: Float): Int {
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

    fun addAll(toAdd: DynFloatArrayRO) {
        for (i in 0 until toAdd.length) {
            if (toAdd.isEmpty(i))
                continue
            add(toAdd[i])
        }
    }

    fun addAll(vararg values: Float) {
        for (value in values)
            add(value)
    }

    fun addAll(floatIterator: FloatIterator?) {
        if (floatIterator == null)
            return
        while (floatIterator.hasNext())
            add(floatIterator.next())
    }

    override fun isEmpty(index: Int): Boolean =
        array[index] == nullValue

    override fun contains(value: Float): Boolean {
        var i = 0
        while (i < array.size) {
            if (array[i] == value)
                return true
            i++
        }
        return false
    }

    fun remove(value: Float): Boolean {
        val indexOf = indexOf(value)
        if (indexOf >= 0) {
            array[indexOf] = nullValue
            size--
            return true
        }

        return false
    }

    fun removeAt(index: Int): Float {
        val result = array[index]
        array[index] = nullValue
        return result
    }

    fun swap(index1: Int, index2: Int) {
        val tmp = array[index1]
        array[index1] = array[index2]
        array[index2] = tmp
    }

    override fun get(index: Int): Float =
        array[index]

    override fun nextIndex(from: Int): Int {
        if (from >= this.size)
            return -1
        var result = from
        while (result < array.size && array[result] == nullValue)
            result++
        if (result >= array.size) return -1
        return result
    }

    override operator fun iterator(): IndexedTypeIterator<Float> =
        IndexedTypeIterator(this)

    private fun firstEmptyIndex(): Int =
        indexOf(nullValue)

    override fun indexOf(value: Float): Int {
        var i = 0
        while (i < array.size) {
            if (array[i] == value)
                return i
            i++
        }

        return -1
    }

    fun trim() {
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
        builder.append("FloatBag [nullValue=").append(nullValue)
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
        array = FloatArray(size)
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