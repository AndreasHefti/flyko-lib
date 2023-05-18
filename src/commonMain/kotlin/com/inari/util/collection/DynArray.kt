/*******************************************************************************
 * Copyright (c) 2015 - 2016 - 2016, Andreas Hefti, inarisoft@yahoo.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inari.util.collection

import com.inari.util.arraycopy
import kotlin.Comparator
import kotlin.jvm.JvmField


interface DynArrayRO<T> : IndexIterable, IndexedTypeIterable<T>, Iterable<T> {

    /** Indicates the grow number that defines the number of additional capacity that is added when a object is set with an
     * index higher then the actual capacity.
     *
     * @return the grow number
     */
    val grow: Int

    /** Returns <tt>true</tt> if this DynArray contains no elements.
     *
     * @return <tt>true</tt> if this DynArray contains no elements
     */
    val isEmpty: Boolean

    /** Get the size of the DynArray. The size is defined by the number of objects that
     * the DynArray contains.
     * NOTE: this is not the same like length of an array which also counts the null/empty values
     */
    val size: Int

    /** Get the capacity of the DynArray. This is the size of the internal ArrayList and is
     * according to the length of an array.
     */
    val capacity: Int

    /** Get the object at specified index or null if there is no object referenced by specified index.
     * If index is out of range ( 0 - capacity ) then an IndexOutOfBoundsException is thrown.
     *
     * @param index the index
     * @throws IndexOutOfBoundsException if index is out of bounds ( 0 - capacity )
     */
    override operator fun get(index: Int): T?

    /** Indicates if there is an object referenced by the specified id. If there is no object referenced
     * by the index, false is returned also in the case, the index is out of bounds.
     *
     * @param index the index
     * @return true if there is an object referenced by the specified id
     */
    operator fun contains(index: Int): Boolean

    /** Use this to get the index of a specified object in the DynArray.
     * This checks only gives back the index of the same object instance (no equals check)
     *
     * @param value The object value to get the associated index
     */
    fun indexOf(value: T): Int

    /** Use this to get the next none empty index of the array from given index
     * @param from the start index where to get the next none empty index of the array
     * @return the next none empty index in the array or -1 if there is no such index
     */
    override fun nextIndex(from: Int): Int

}

/** An Array that dynamically grows if more space is needed.
 *
 * Since the creation of a typed Array need the class of the type to properly create the array and since this implementation wants to
 * avoid a cast on every get, a DynArray has do be instantiated within defined static create methods.
 * <P>
 * DynArray<String> dynArray = DynArray.create( String.class )
</String></P> * <P For example will create a DynArray></P><String> with a initial capacity of 50 and a grow factor of 20
 * <P>
 * If a DynArray has to be created for a type that itself is typed, use the createType method..
</P> * <P>
 * DynArray<List></List><String> dynArray = DynArray.createTyped( List.class )
 *
 * @param <T> The type of objects in the DynArray
 * @param initialCapacity The internal ArrayList is initialized with
 * initialCapacity + ( initialCapacity / 2 ) and filled up with null values for the indexes to initialCapacity.
</T></String></P></String> */
class DynArray<T> constructor(
    initialCapacity: Int = 10,
    override val grow: Int,
    val arrayAllocation: (Int) -> Array<T?>
) : DynArrayRO<T> {

    @JvmField var array: Array<T?> = arrayAllocation(initialCapacity)

    override var size: Int = 0
        private set

    override val capacity: Int
        get() = array.size


    init { createList(initialCapacity) }

    /** Returns <tt>true</tt> if this DynArray contains no elements.
     *
     * @return <tt>true</tt> if this DynArray contains no elements
     */
    override val isEmpty: Boolean
        get() = size <= 0


    /** Sets the value at the specified position of the DynArray and returns the value that was at the position before
     * or null of the position was empty. If the index is out of the range of the current capacity of DynArray, the
     * internal ArrayList will be resized to fit the new index. This is the only case where this method will take
     * some more performance.
     *
     * @param index the index to set the value
     * @param value the value to set
     * @return the value that was at the position before or null of the position was empty
     */
    operator fun set(index: Int, value: T?): T? {
        ensureCapacity(index)
        val old = array[index]
        if (value != null)
            size++
        array[index] = value!!
        return old
    }

    /** Add the specified value at the first empty (null) position that is found in the DynArray.
     * If there is no empty position the list is growing by the grow value and the value is added
     * to at the end of old list.
     *
     * @param value The value to add to the DynArray
     * @return the index of the newly added value
     */
    fun add(value: T): Int {
        var index = 0
        while (index < array.size && array[index] != null)
            index++
        set(index, value)
        return index
    }

    /** Add the specified value at the first empty (null) position that is found in the DynArray.
     * If there is no empty position the list is growing by the grow value and the value is added
     * to at the end of old list.
     *
     * @param value The value to add to the DynArray
     * @return the index of the newly added value
     */
    operator fun plus(value: T): Int = add(value)

    /** Use this to add all values of a DynArray to the instance of DynArray.
     *
     * @param values the other DynArray to get the values form
     */
    fun addAll(values: DynArrayRO<T>) {
        ensureCapacity(this.size + values.size)
        var i = values.nextIndex(0)
        while (i >= 0) {
            add(values[i]!!)
            i = values.nextIndex(i + 1)
        }
    }

    /** Get the object at specified index or null if there is no object referenced by specified index.
     * If index is out of range ( 0 - capacity ) then an IndexOutOfBoundsException is thrown.
     *
     * @param index the index
     * @throws IndexOutOfBoundsException if index is out of bounds ( 0 - capacity )
     */
    override operator fun get(index: Int): T? =
        array[index]

    /** Indicates if there is an object referenced by the specified id. If there is no object referenced
     * by the index, false is returned also in the case, the index is out of bounds.
     *
     * @param index the index
     * @return true if there is an object referenced by the specified id
     */
    override operator fun contains(index: Int): Boolean =
        if (index < 0 || index >= array.size) false
        else array[index] != null

    /** Use this to get the index of a specified object in the DynArray.
     * This checks only gives back the index of the same object instance (no equals check) that was first found in the array.
     * If there are more reference to the same instance within an DynArray, this will only return the index of the first match
     *
     * @param value The object value to get the associated index
     */
    override fun indexOf(value: T): Int {
        if (value == null)
            return -1
        var i = 0
        while (i < capacity) {
            if (value === array[i])
                return i
            i++
        }

        return -1
    }

    /** Use this to get the next none empty index of the array from given index
     * @param from the start index where to get the next none empty index of the array
     * @return the next none empty index in the array or -1 if there is no such index
     */
    override fun nextIndex(from: Int): Int {
        if (from >= this.capacity)
            return -1
        var result = from
        while (result < array.size && array[result] == null)
            result++
        if (result >= array.size) return -1
        return result
    }

    /** Removes the object on specified index of DynArray and returns the objects that was set before.
     *
     * @param index the index
     * @return The objects that was set before or null of there was none
     * @throws IndexOutOfBoundsException if index is out of bounds ( 0 - capacity )
     */
    fun remove(index: Int): T? {
        if (index < 0 || index >= array.size)
            return null
        val result = array[index]
        array[index] = null
        size--
        return result
    }

    fun removeAll(indices: BitSet) {
        val iterator = IndexIterator(indices)
        while (iterator.hasNext())
            remove(iterator.nextInt())
    }

    /** This removes all given equal instances to a given value instance from the DynArray.
     * Use equals to check equality and sets a null value on specified index where an equal instance is found
     *
     * @param value The value for that all given equal value instances are removed from the DynArray
     * @return the number of removed instances.
     */
    fun removeAll(value: T?): Int {
        if (value == null)
            return 0

        var number = 0
        var i = 0
        while (i < capacity) {
            if (value == array[i]) {
                remove(i)
                number++
            }
            i++
        }

        return number
    }

    /** Removes the first found specified instance of object value from DynArray and returns the index where it as removed.
     * Also this remove sets a null value on specified index of internal ArrayList instead of removing it
     * to avoid the index shift of an ArrayList remove.
     *
     * @param value The value to remove.
     * @return the index of the value that was removed or -1 if there was no such value.
     */
    fun remove(value: T): Int {
        val indexOf = indexOf(value)
        if (indexOf >= 0)
            remove(indexOf)
        return indexOf
    }

    /** Removes the first found specified instance of object value from DynArray and returns the index where it as removed.
     * Also this remove sets a null value on specified index of internal ArrayList instead of removing it
     * to avoid the index shift of an ArrayList remove.
     *
     * @param value The value to remove.
     * @return the index of the value that was removed or -1 if there was no such value.
     */
    operator fun minus(value: T): Int = remove(value)


    /** Sorts the list within the given comparator.
     * @param comparator
     */
    fun sort(comparator: Comparator<T?>) = array.sortWith(comparator)

    /** Clears the whole list, removes all objects and sets the capacity to 0.
     */
    fun clear() {
        var i = 0
        while (i < capacity) {
            array[i] = null
            i++
        }
        size = 0
    }

    /** Gets an Iterator of specified type to iterate over all objects in the DynArray
     * by skipping the empty/null values. This does not concurrent modification checks at all
     * but is not that fast like an array iteration or by using the listIterator but skips
     * the null values.
     *
     * @return an Iterator of specified type to iterate over all objects in the DynArray by skipping the empty/null values
     */
    override fun iterator(): Iterator<T> = IndexedTypeIterator(this)

    fun trim() {
        if (size == capacity)
            return

        val oldArray = array
        array = arrayAllocation(size)
        var index = 0
        var i = 0
        while (i < capacity) {
            if (oldArray[i] != null) {
                array[index] = oldArray[i]
                index++
            }
            i++
        }
    }


    fun toArray(): Array<T> {
        @Suppress("UNCHECKED_CAST")
        val result: Array<T> = arrayAllocation(size) as Array<T>
        var index = 0
        var i = 0
        while (i < capacity) {
            if (array[i] != null) {
                result[index] = array[i]!!
                index++
            }
            i++
        }
        return result
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("DynArray [list=")
        builder.append(array.contentToString())
        builder.append(", size=")
        builder.append(size)
        builder.append(", capacity=")
        builder.append(capacity)
        builder.append("]")
        return builder.toString()
    }

    fun ensureCapacity(index: Int) {
        if (this === NULL_ARRAY)
            throw RuntimeException("EMPTY_ARRAY is immutable")

        if (index < array.size)
            return

        val size = array.size
        var newSize = size

        while (index >= newSize)
            newSize += grow

        val oldArray = array
        array = arrayAllocation(newSize)
        arraycopy(oldArray, 0, array, 0, oldArray.size)
    }

    private fun createList(initialCapacity: Int) {
        array = if (initialCapacity > 0)
            arrayAllocation(initialCapacity)
        else
            arrayAllocation(0)
    }

    companion object {

        @JvmField val NULL_ARRAY: DynArrayRO<Any> = of(0, 0)
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> nullArray() : DynArray<T> = NULL_ARRAY as DynArray<T>

        inline fun <reified T> of(): DynArray<T> {
            return DynArray(50, 20) { size -> arrayOfNulls<T?>(size) }
        }

        inline fun <reified T> of(initialCapacity: Int): DynArray<T> {
            return DynArray( initialCapacity, 20) { size -> arrayOfNulls<T?>(size) }
        }

        inline fun <reified T> of(initialCapacity: Int, grow: Int): DynArray<T> {
            return DynArray(initialCapacity, grow) { size -> arrayOfNulls<T?>(size) }
        }

        inline fun <reified T> fromArray(array: Array<T>, grow: Int): DynArray<T> {
            val result = DynArray(array.size, grow) { size -> arrayOfNulls<T?>(size) }
            for (i in array.indices) {
                if (array[i] != null) {
                    result[i] = array[i]
                }
            }

            return result
        }
    }

}
