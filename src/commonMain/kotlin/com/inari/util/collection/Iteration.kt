package com.inari.util.collection

interface IndexIterable {
    fun nextIndex(from: Int): Int
}

interface IntListIterable  {
    fun nextListIndex(from: Int): Int
    operator fun get(index: Int): Int
}

interface IndexedTypeIterable<T> : IndexIterable  {
    operator fun get(index: Int): T?
}

class IndexIterator private constructor(private var ref: IndexIterable) {

    private var nextIndex = ref.nextIndex(0)
    fun hasNext(): Boolean = nextIndex >= 0
    fun nextInt(): Int {
        val i = nextIndex
        nextIndex = ref.nextIndex(nextIndex + 1)
        if (!hasNext())
            iteratorPool.add(this)
        return i
    }
    private fun reset(ref: IndexIterable) {
        this.ref = ref
        nextIndex = ref.nextIndex(0)
        if (!hasNext())
            iteratorPool.add(this)
    }

    companion object {

        private val iteratorPool = ArrayDeque<IndexIterator>()

        operator fun invoke(ref: IndexIterable): IndexIterator {
            if (iteratorPool.isEmpty())
                for (i in 0 .. 10)
                    iteratorPool.add(IndexIterator(ref))

            val n: IndexIterator = iteratorPool.removeFirst()
            //DEBUG println("*********** reuse old IndexIterator ${n.hashCode()} queueSize: ${iteratorPool.size}")
            n.reset(ref)
            return n
        }
    }
}

class IndexListIterator private constructor(private var ref: IntListIterable) {

    var nextIndex = ref.nextListIndex(0)
    fun hasNext(): Boolean = nextIndex >= 0
    fun nextInt(): Int {
        val i = nextIndex
        nextIndex = ref.nextListIndex(nextIndex + 1)
        if (!hasNext())
            iteratorPool.add(this)
        return ref[i]
    }
    private fun reset(ref: IntListIterable) {
        this.ref = ref
        nextIndex = ref.nextListIndex(0)
        if (!hasNext())
            iteratorPool.add(this)
    }

    companion object {

        private val iteratorPool = ArrayDeque<IndexListIterator>()

        operator fun invoke(ref: IntListIterable): IndexListIterator {
            if (iteratorPool.isEmpty())
                for (i in 0 .. 10)
                    iteratorPool.add(IndexListIterator(ref))

            val n: IndexListIterator = iteratorPool.removeFirst()
            //DEBUG println("*********** reuse old IndexIterator ${n.hashCode()} queueSize: ${iteratorPool.size}")
            n.reset(ref)
            return n
        }
    }
}

class IndexedTypeIterator<T> private constructor(private var ref: IndexedTypeIterable<T>) : Iterator<T> {

    var nextIndex = ref.nextIndex(0)
    override fun hasNext(): Boolean = nextIndex >= 0
    override fun next(): T {
        val r = ref[nextIndex]!!
        nextIndex = ref.nextIndex(nextIndex + 1)
        if (!hasNext())
            iteratorPool.add(this)
        return r
    }
    private fun reset(ref: IndexedTypeIterable<T>) {
        this.ref = ref
        nextIndex = ref.nextIndex(0)
        if (!hasNext())
            iteratorPool.add(this)
    }

    companion object {

        private val iteratorPool = ArrayDeque<IndexedTypeIterator<*>>()

        @Suppress("UNCHECKED_CAST")
        operator fun <T> invoke(ref: IndexedTypeIterable<T>): IndexedTypeIterator<T> {
            if (iteratorPool.isEmpty()) {
                for (i in 0 .. 10)
                    iteratorPool.add(IndexedTypeIterator(ref))
                return IndexedTypeIterator(ref)
            }

            val n: IndexedTypeIterator<T> = iteratorPool.removeFirst() as IndexedTypeIterator<T>
            //DEBUG println("*********** reuse old IndexedTypeIterator ${n.hashCode()} queueSize: ${iteratorPool.size}")
            n.reset(ref)
            return n
        }
    }
}