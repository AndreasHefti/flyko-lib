package com.inari.util.collection

interface Dictionary : Iterable<String> {
    operator fun contains(name: String): Boolean
    operator fun get(name: String): String?
    operator fun plus(dic: Dictionary): Dictionary
    fun names(): Iterator<String>
    override fun iterator(): Iterator<String> = names()
}
val EMPTY_DICTIONARY: Dictionary = object : Dictionary {
    override fun contains(name: String): Boolean = false
    override fun get(name: String): String? = null
    override fun plus(dic: Dictionary): Dictionary = dic
    override fun names(): Iterator<String> = emptyList<String>().iterator()
}
class Attributes(private val map: MutableMap<String, String> = mutableMapOf()) : Dictionary {
    override fun contains(name: String): Boolean = map.containsKey(name)
    override fun get(name: String): String? = map[name]
    operator fun plus(pair: Pair<String, String>): Attributes {
        map[pair.first] = pair.second
        return this
    }
    override fun plus(dic: Dictionary): Dictionary {
        val newMap = map.toMutableMap()
        if (dic is Attributes)
            newMap.putAll(dic.map)
        else
            dic.forEach { newMap[it] = dic[it]!! }
        return Attributes(newMap)
    }
    override fun names(): Iterator<String> = map.keys.iterator()
}