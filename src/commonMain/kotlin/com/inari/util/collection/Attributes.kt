package com.inari.util.collection

import com.inari.firefly.game.NULL_VALUE
import com.inari.util.EMPTY_STRING
import com.inari.util.KEY_VALUE_SEPARATOR
import com.inari.util.LIST_VALUE_SEPARATOR
import kotlin.jvm.JvmField

interface AttributesRO : Iterable<String> {
    operator fun contains(name: String): Boolean
    operator fun get(name: String): String?
    fun names(): Iterator<String>
    override fun iterator(): Iterator<String> = names()
    fun getBoolean(name: String): Boolean

    companion object {
        @JvmField val EMPTY_ATTRIBUTES: AttributesRO = Attributes()
    }
}
class Attributes : AttributesRO {
    private val map: MutableMap<String, String> = HashMap(5)

    override fun contains(name: String): Boolean = map.containsKey(name)
    override fun get(name: String): String? = map[name]
    operator fun plus(pair: Pair<String, String>): Attributes {
        map[pair.first] = pair.second
        return this
    }
    operator fun plus(dic: AttributesRO): Attributes {
        if (dic is Attributes)
            this.map.putAll(dic.map)
        else {
            val iter = dic.iterator()
            while (iter.hasNext()) {
                val n = iter.next()
                this.map[n] = dic[n]!!
            }
        }
        return this
    }
    operator fun set(name: String, value: String) {
        map[name] = value
    }
    operator fun set(prefix: String, dic: AttributesRO): Attributes {
        val iter = dic.iterator()
        while (iter.hasNext()) {
            val n = iter.next()
            this.map[prefix + n] = dic[n]!!
        }
        return this
    }
    fun addAll(jsonString: String): Attributes {
        if (EMPTY_STRING == jsonString || NULL_VALUE == jsonString) return this
        val attrs = jsonString.split(LIST_VALUE_SEPARATOR)
        val it = attrs.iterator()
        while (it.hasNext()) {
            val attr = it.next().split(KEY_VALUE_SEPARATOR)
            this[attr[0]] = attr[1]
        }
        return this
    }
    override fun names(): Iterator<String> = map.keys.iterator()
    override fun getBoolean(name: String) =
        map[name]?.toBoolean() ?: false

    fun clear() = map.clear()

    fun toJsonString(): String {
        if (map.isEmpty())
            return EMPTY_STRING

        var result = EMPTY_STRING
        val it = map.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (result == EMPTY_STRING)
                result = "${entry.key}$KEY_VALUE_SEPARATOR${entry.value}"
            else
                result += "$LIST_VALUE_SEPARATOR${entry.key}$KEY_VALUE_SEPARATOR${entry.value}"
        }
        return result
    }

    override fun toString(): String = map.toString()
}