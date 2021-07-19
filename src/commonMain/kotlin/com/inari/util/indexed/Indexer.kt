package com.inari.util.indexed

import com.inari.util.StringUtils.EMPTY_STRING
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField


interface Indexed {
    val index: Int
    val indexedTypeName: String
}

abstract class AbstractIndexed(
    final override val indexedTypeName: String,
    val subTypeName: String = EMPTY_STRING,
    applyIndex: Boolean = true
) : Indexed {

    @JvmField internal var iindex: Int = -1

    init {
        if (applyIndex)
            applyNewIndex()
    }

    final override val index: Int
        get() = iindex

    protected fun disposeIndex() =
        Indexer.of(indexedTypeName).disposeIndex(this)
    protected fun applyNewIndex() =
        Indexer.of(indexedTypeName).applyNewIndex(this)
    protected fun finalize() =
        disposeIndex()
}

class Indexer private constructor(
    private val name: String
) {

    private val indices: BitSet = BitSet()
    private val subTypeNames: DynArray<String> = DynArray.of()

    fun applyNewIndex(indexedSupplier: () -> AbstractIndexed) =
        applyNewIndex(indexedSupplier())

    fun applyNewIndex(indexed: AbstractIndexed) {
        typeCheck(indexed)

        if (indexed.index >= 0)
            disposeIndex(indexed)

        indexed.iindex = indices.nextClearBit(0)
        indices.set(indexed.iindex)
        if (EMPTY_STRING !== indexed.subTypeName)
            subTypeNames[indexed.iindex] = indexed.subTypeName
    }

    fun disposeIndex(indexedSupplier: () -> AbstractIndexed) =
        disposeIndex(indexedSupplier())

    fun disposeIndex(indexed: AbstractIndexed) {
        typeCheck(indexed)

        if (indexed.index >= 0) {
            indices.clear(indexed.index)
            subTypeNames.remove(indexed.index)
        }

        indexed.iindex = -1
    }

    fun clear() =
        indices.clear()

    private fun typeCheck(indexed: AbstractIndexed) {
        if (indexed.indexedTypeName != this.name)
            throw IllegalArgumentException("Indexer with name: $name deny indexing of type: ${indexed.indexedTypeName}")
    }

    override fun toString(): String =
        "Indexer(name='$name', indices=$indices)"

    fun toDumpString(): String {
        val builder = StringBuilder()
        var i = indices.nextSetBit(0)
        while (i >= 0) {
            builder.append("\n    $i")
            if (i in subTypeNames) {
                builder.append(":${subTypeNames[i]}")
            }

            i = indices.nextSetBit(i+1)
        }
        return builder.toString()
    }



    companion object {
        private val indexer: LinkedHashMap<String, Indexer> = LinkedHashMap()

        fun dump(): String {
            val builder = StringBuilder()
            builder.append("Indexer : {")
            for ((key, value) in indexer) {
                builder.append("\n  ").append(key).append(" : ").append(value.toDumpString())
            }
            builder.append("\n}")
            return builder.toString()
        }
        fun dump(indexerName: String): String {
            val builder = StringBuilder()
            builder.append("$indexerName : {")
            if (indexerName in indexer) {
                builder.append(indexer[indexerName]!!.toDumpString())
            }
            builder.append("\n}")
            return builder.toString()
        }

        fun clearAll() {
            for(ind in indexer.values)
                ind.clear()
            indexer.clear()
        }


        fun of(name: String, override: Boolean = false): Indexer =
            if (name in indexer)
                if(override) createNew(name)
                else indexer[name]!!
            else createNew(name)

        private fun createNew(name: String): Indexer {
            indexer[name] = Indexer(name)
            return indexer[name]!!
        }
    }
}