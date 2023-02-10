package com.inari.util.collection

interface IndexIterable {
    fun nextIndex(from: Int): Int
}

interface IndexedTypeIterable<T> :IndexIterable  {
    operator fun get(index: Int): T?
}

class IndexIterator(private var ref: IndexIterable) : IntIterator() {

    var nextIndex = ref.nextIndex(0)
    override fun hasNext(): Boolean = nextIndex >= 0
    override fun nextInt(): Int {
        val r = nextIndex
        nextIndex = ref.nextIndex(nextIndex + 1)
        return r
    }
    private fun reset(ref: IndexIterable) {
        this.ref = ref
        nextIndex = ref.nextIndex(0)
    }

    companion object {
        private val iteratorPool = DynArray.of<IndexIterator>(2, 2)
        operator fun invoke(ref: IndexIterable) = getIndexIterator(ref)
        fun getIndexIterator(ref: IndexIterable): IndexIterator {
            var i = iteratorPool.nextIndex(0)
            while (i >= 0) {
                val n = iteratorPool[i]!!
                if (!n.hasNext()) {
                    n.reset(ref)
                    // DEBUG  println("*********** reuse old IndexIterator")
                    return n
                }
                i = iteratorPool.nextIndex(i + 1)
            }
            // DEBUG  println("******** create new IndexIterator")
            val r = IndexIterator(ref)
            iteratorPool.add(r)
            return r
        }
    }
}

class IndexedTypeIterator<T> (private var ref: IndexedTypeIterable<T>) : Iterator<T> {
    var nextIndex = ref.nextIndex(0)
    override fun hasNext(): Boolean = nextIndex >= 0
    override fun next(): T {
        val r = nextIndex
        nextIndex = ref.nextIndex(nextIndex + 1)
        return ref[r]!!
    }
    private fun reset(ref: IndexedTypeIterable<T>) {
        this.ref = ref
        nextIndex = ref.nextIndex(0)
    }

    companion object {
        private val iteratorPool = DynArray.of<IndexedTypeIterator<*>>(2, 2)
        @Suppress("UNCHECKED_CAST")
        fun <T> getIndexIterator(ref: IndexedTypeIterable<T>): IndexedTypeIterator<T> {
            var i = iteratorPool.nextIndex(0)
            while (i >= 0) {
                val n: IndexedTypeIterator<T> = iteratorPool[i]!! as IndexedTypeIterator<T>
                if (!n.hasNext()) {
                    n.reset(ref)
                    // DEBUG  println("*********** reuse old IndexedTypeIterator")
                    return n
                }
                i = iteratorPool.nextIndex(i + 1)
            }
            // DEBUG  println("******** create new IndexedTypeIterator<T>")
            val r = IndexedTypeIterator(ref)
            iteratorPool.add(r)
            return r
        }
    }

}