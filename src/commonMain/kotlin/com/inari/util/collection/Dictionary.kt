package com.inari.util.collection

interface Dictionary : Iterable<String> {
    operator fun contains(name: String): Boolean
    operator fun get(name: String): String?
    operator fun plus(dic: Dictionary): Dictionary
    operator fun set(prefix: String, dic: Dictionary): Dictionary
    fun names(): Iterator<String>
    override fun iterator(): Iterator<String> = names()
}
val EMPTY_DICTIONARY: Dictionary = object : Dictionary {
    override fun contains(name: String): Boolean = false
    override fun get(name: String): String? = null
    override fun plus(dic: Dictionary): Dictionary = dic
    override fun set(prefix: String, dic: Dictionary): Dictionary = throw UnsupportedOperationException()
    override fun names(): Iterator<String> = emptyList<String>().iterator()
}
class Attributes() : Dictionary {
    private val map: MutableMap<String, String> = HashMap(5)

    override fun contains(name: String): Boolean = map.containsKey(name)
    override fun get(name: String): String? = map[name]
    operator fun plus(pair: Pair<String, String>): Attributes {
        map[pair.first] = pair.second
        return this
    }
    override fun plus(dic: Dictionary): Dictionary {
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
    override fun set(prefix: String, dic: Dictionary): Dictionary {
        val iter = dic.iterator()
        while (iter.hasNext()) {
            val n = iter.next()
            this.map[prefix + n] = dic[n]!!
        }
        return this
    }
    override fun names(): Iterator<String> = map.keys.iterator()
    override fun toString(): String = map.toString()
}