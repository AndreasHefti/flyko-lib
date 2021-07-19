package com.inari.util.collection

/** The advantages of ArrayList and an Array in one.
 * Use StaticList if a good performance matters and just a simple indexed storage is needed to set and get objects from.
 *
 * Internally this is implemented within an ArrayList but with fixed positions (indices) of stored objects like
 * an Array. An ArrayList has good performance on get and add calls but not an add( index ) and remove( index )
 * calls because of the index shift that is needed and ArrayList has no good performance on iteration because
 * there are a couple of concurrent modification checks done while ArrayList iteration.
 *
 * StaticList has no add methods but set( index ) method like an array and if there is already an object referenced
 * by this index, the referenced is overwritten with the new object reference like in an array.
 * The remove( index ) method use first a get of the already referenced object and then set a null value at the
 * specified position to avoid index shifting.
 *
 * StaticList uses an Iterator implementation using index without concurrent modification checks and should be as fast as
 * an array iteration. But StaticList is not synchronized and do not check concurrent modifications in any case.
 *
 * If the index to set is higher then the current capacity of the internal ArrayList the internal ArrayList is
 * been automatically growing like a normal ArrayList and filled with null values to fit the new capacity.
 * The capacity of the list (and the internal ArrayList) never shrink while just remove objects. Only the clear
 * functions removes all objects and also clears the internal ArrayList.
 *
 * @param <T> The type of objects in the StaticList
</T> */
class StaticList<T>(
    val initialCapacity: Int = 10,
    val grow: Int = 10
) : Iterable<T> {

    private var list: ArrayList<T?> = ArrayList(initialCapacity)

    init {
        createList(initialCapacity)
    }

    /** Get the size of the StaticList. The size is defined by the number of objects that
     * the StaticList contains.
     * NOTE: this is not the same like length of an array which also counts the null/empty values
     */
    var size = 0
        private set



    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    val isEmpty: Boolean
        get() = size <= 0

    /** Sets the value at the specified position of the StaticList and returns the value that was at the position before
     * or null of the position was empty. If the index is out of the range of the current capacity of StaticList, the
     * internal ArrayList will be resized to fit the new index. This is the only case where this method will take
     * some more performance.
     *
     * @param index the index to set the value
     * @param value the value to set
     * @return the value that was at the position before or null of the position was empty
     */
    operator fun set(index: Int, value: T?): T? {
        ensureCapacity(index)
        val old = list[index]
        list[index] = value
        if (value != null) {
            size++
        }
        return old
    }

    /** Add the specified value at the first empty (null) position that is found in the StaticList.
     * If there is no empty position the list is growing by the grow value and the value is added
     * to at the end of old list.
     *
     * @param value The value to add to the StaticList
     * @return the index of the newly added value
     */
    fun add(value: T): Int {
        var index = 0
        while (index < list.size && list[index] != null) {
            index++
        }
        set(index, value)
        return index
    }

    /** Use this to add all values of a StaticList to the instance of StaticList.
     *
     * @param values the other StaticList to get the values form
     */
    fun addAll(values: StaticList<T>) {
        ensureCapacity(this.size + values.size)
        for (value in values) {
            add(value)
        }
    }

    /** Get the object at specified index or null if there is no object referenced by specified index.
     * If index is out of range ( 0 - capacity ) then an IndexOutOfBoundsException is thrown.
     *
     * @param index the index
     * @throws IndexOutOfBoundsException if index is out of bounds ( 0 - capacity )
     */
    operator fun get(index: Int): T? =
        list[index]

    /** Indicates if there is an object referenced by the specified id. If there is no object referenced
     * by the index, false is returned also in the case, the index is out of bounds.
     *
     * @param index the index
     * @return true if there is an object referenced by the specified id
     */
    operator fun contains(index: Int): Boolean {
        return if (index < 0 || index >= list.size) {
            false
        } else list[index] != null
    }

    /** Use this to check if a certain value is already contained by this StaticList.
     * Uses == and equals to check equality
     * @param value The value to check
     * @return true if a certain value is already contained by this StaticList
     */
    operator fun contains(value: T): Boolean {
        for (i in 0 until capacity()) {
            val v = list[i] ?: continue

            if (v === value) {
                return true
            }
        }

        return false
    }

    /** Use this to get the index of a specified object in the StaticList. This inernally uses
     * ArrayList.indexOf and has the same performance.
     *
     * @param value The object value to get the associated index
     */
    fun indexOf(value: T): Int =
        list.indexOf(value)

    /** Removes the object on specified index of StaticList and returns the objects that was set before.
     *
     * @param index the index
     * @return The objects that was set before or null of there was none
     * @throws IndexOutOfBoundsException if index is out of bounds ( 0 - capacity )
     */
    fun remove(index: Int): T? {
        if (!contains(index)) {
            return null
        }

        val result = list[index]
        list[index] = null
        size--
        return result
    }

    /** Removes the specified object value from StaticList and returns the index where it as removed.
     * Also this remove sets a null value on specified index of internal ArrayList instead of removing it
     * to avoid the index shift of an ArrayList remove.
     *
     * @param value The value to remove.
     * @return the index of the value that was removed or -1 if there was no such value.
     */
    fun remove(value: T): Int {
        val indexOf = list.indexOf(value)
        if (indexOf >= 0) {
            remove(indexOf)
        }
        return indexOf
    }

    /** Sorts the list within the given comparator.
     * @param comparator
     */
    fun sort(comparator: Comparator<T?>) = list.sortWith(comparator)

    /** Get the capacity of the StaticList. This is the size of the internal ArrayList and is
     * according to the length of an array.
     */
    fun capacity(): Int = list.size

    /** Clears the whole list, removes all objects and sets the capacity to 0.
     */
    fun clear() {
        for (i in list.indices) {
            list[i] = null
        }
        size = 0
    }

    /** Gets the iterator from internal list. NOTE: this also gets the null values in the internal
     * list but is as fast as a normal ArrayList iterator.
     *
     * @return the iterator from internal list
     */
    fun listIterator(): Iterator<T?> = list.iterator()

    /** Gets an Iterator of specified type to iterate over all objects in the StaticList
     * by skipping the empty/null values. This does not concurrent modification checks at all
     * but is not that fast like an array iteration or by using the listIterator but skips
     * the null values.
     *
     * @return an Iterator of specified type to iterate over all objects in the StaticList by skipping the empty/null values
     */
    override fun iterator(): Iterator<T> = StaticListIterator()

    /** Use this to get an Array of specified type from this StaticList.
     * The Array has the length of the size of the StaticList but the indexes may change if there are null references
     * in this StaticList that are not at the end of the list.
     * So this gets a packed array of the list representing within this StaticList. If you need the exact array representation
     * with the null references and the exact indices, use toExactArray method.
     *
     * @return an Array of specified type from this StaticList.
     */
    fun toArray(): Array<T> {
        @Suppress("UNCHECKED_CAST")
        val result: Array<T> = arrayOfNulls<Any?>(list.size) as Array<T>
        var index = 0
        for (value in list) {
            if (value != null) {
                result[index] = value
                index++
            }
        }
        return result
    }

    /** Use this to get an exact Array representation of this StaticList instance.
     * This contains also all null references and the indices has no change to the StaticList.
     * @return an exact Array representation of this StaticList instance.
     */
    fun toExactArray(): Array<T?> {
        @Suppress("UNCHECKED_CAST")
        val result: Array<T?> = arrayOfNulls<Any?>(list.size) as Array<T?>
        for (i in list.indices) {
            result[i] = list[i]
        }
        return result
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("StaticList [list=")
        builder.append(list)
        builder.append(", size=")
        builder.append(size)
        builder.append(", capacity=")
        builder.append(capacity())
        builder.append("]")
        return builder.toString()
    }

    private fun ensureCapacity(index: Int) {
        var size = list.size
        var newSize = size
        while (index >= newSize)
            newSize += grow
        while (size < newSize) {
            list.add(null)
            size++
        }
    }

    private fun createList(initialCapacity: Int) {
        list = ArrayList(initialCapacity + initialCapacity / 2)
        for (i in 0 until initialCapacity)
            list.add(null)
    }

    companion object {
        private val empty: StaticList<*> = StaticList<Any>()
        @Suppress("UNCHECKED_CAST")
        fun <T>  emtpyList(): StaticList<T> = empty as StaticList<T>
    }

    // TODO optimization: get rid of list.get in next or findNext.
    private inner class StaticListIterator : Iterator<T> {

        private var index = 0
        private val size = list.size

        init {
            findNext()
        }

        override fun hasNext(): Boolean {
            return index < list.size
        }

        override fun next(): T {
            val result: T = list[index]!!
            index++
            findNext()
            return result
        }

        private fun findNext() {
            while (index < size && list[index] == null)
                index++
        }
    }
}
