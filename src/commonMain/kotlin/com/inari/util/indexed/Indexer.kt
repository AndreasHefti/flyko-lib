package com.inari.util.indexed

import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField


interface Indexed {
    val index: Int
    val indexedTypeName: String
}

abstract class AbstractIndexed(
    final override val indexedTypeName: String,
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

    fun applyNewIndex(indexedSupplier: () -> AbstractIndexed) =
        applyNewIndex(indexedSupplier())

    fun applyNewIndex(indexed: AbstractIndexed) {
        typeCheck(indexed)

        if (indexed.index >= 0)
            throw IllegalArgumentException("Index already applied: ${indexed.index}")

        indexed.iindex = indices.nextClearBit(0)
        indices.set(indexed.iindex)
    }

    fun disposeIndex(indexedSupplier: () -> AbstractIndexed) =
        disposeIndex(indexedSupplier())

    fun disposeIndex(indexed: AbstractIndexed) {
        typeCheck(indexed)

        if (indexed.index >= 0)
            indices.clear(indexed.index)

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
        return indices.cardinality.toString()
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
            builder.append("$indexerName : ")
            if (indexerName in indexer)
                builder.append(indexer[indexerName]!!.toDumpString())
            else
                builder.append("0")
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