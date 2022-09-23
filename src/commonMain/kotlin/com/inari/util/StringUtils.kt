package com.inari.util

import com.inari.util.collection.BitSet
import kotlin.math.pow
import kotlin.math.roundToInt

/** Defines utility methods for String manipulation
 *
 * @author andreas hefti
 */
object StringUtils {

    /** Indicates if the String s is null or an empty String
     * @param s the String
     * @return true if the String s is null or an empty String
     */
    fun isEmpty(s: String?): Boolean =
        s == null || s.isEmpty()

    /** Indicates if the String s is null, an empty String or a blank String
     * @param s the String
     * @return true if the String s is null, an empty String or a blank String
     */
    fun isBlank(s: String?): Boolean =
        s == null || s.trim().isEmpty()

    fun splitToMap(string: String, separator: Char, keyValueSeparator: String): Map<String, String> =
        string.split(separator).associate {
            val index = it.indexOf(keyValueSeparator)
            it.substring(0, index) to it.substring(index + 1, it.length)
        }

    fun fillPrepending(string: String?, fill: Char, length: Int): String? {
        if (string == null)
            return null
        if (string.length >= length)
            return string

        val sb = StringBuilder(string)
        while (sb.length < length)
            sb.insert(0, fill)
        return sb.toString()
    }

    fun fillAppending(string: String?, fill: Char, length: Int): String? {
        if (string == null)
            return null
        if (string.length >= length)
            return string

        val sb = StringBuilder(string)
        while (sb.length < length)
            sb.append(fill)
        return sb.toString()
    }

    fun escapeSeparatorKeys(value: String): String {
        // TODO
        return value
    }

    fun array2DToString(grid: Array<IntArray>?): String? {
        if (grid == null)
            return null

        val sb = StringBuilder()
        sb.append("[")
        for (i in grid.indices)
            sb.append(grid[i].contentToString())
        sb.append("]")
        return sb.toString()
    }

    fun join(collection: Collection<*>?, separatorString: String): String? {
        if (collection == null)
            return null
        val result = StringBuilder()

        val iterator = collection.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val strValue = next?.toString() ?: ""
            // TODO escape separator character

            result.append(strValue)
            if (iterator.hasNext()) {
                result.append(separatorString)
            }
        }

        return result.toString()
    }

    fun bitSetFromString(pixelsString: String): BitSet {
        val pixels = BitSet(pixelsString.length)
        var index = 0
        while (index < pixelsString.length) {
            val c = pixelsString[index]
            if (c == '\n') {
                continue
            }

            pixels[index] = c != '0'
            index++
        }
        return pixels
    }

    fun bitSetToString(bitSet: BitSet, width: Int, height: Int): String {
        val builder = StringBuilder()
        var y = 0
        while (y < height) {
            var x = 0
            while (x < width) {
                builder.append(if (bitSet[y * width + x]) 1 else 0)
                x++
            }
            if (y < height - 1)
                builder.append("\n")
            y++
        }
        return builder.toString()
    }

    // TODO remove this when the kotlin guys implemented this:
    // https://youtrack.jetbrains.com/issue/KT-21644
    fun formatFloat(float: Float, decimal:Int) : String {
        val factor = 10f.pow(decimal)
        val cut = (float * factor).roundToInt() / factor
        var string = cut.toString()
        val decIndex = string.indexOf(".")
        if (decIndex > 0)
            while (string.length - decIndex <= decimal)
                string += "0"
        return string
    }

}
