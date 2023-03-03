package com.inari.util.collection

interface IndexIterable {
    fun nextIndex(from: Int): Int
}

interface IndexedTypeIterable<T> :IndexIterable  {
    operator fun get(index: Int): T?
}

class IndexIterator private constructor(private var ref: IndexIterable) : IntIterator() {

    init {
        println("IndexIterator")
    }

    var nextIndex = ref.nextIndex(0)
    override fun hasNext(): Boolean = nextIndex >= 0
    override fun nextInt(): Int {
        val r = nextIndex
        nextIndex = ref.nextIndex(nextIndex + 1)
        if (!hasNext())
            iteratorPool.add(this)
        return r
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
            //println("*********** reuse old IndexIterator ${n.hashCode()} queueSize: ${iteratorPool.size}")
            n.reset(ref)
            return n
        }
//        private val iteratorPool = DynArray.of<IndexIterator>(2, 2)
//        operator fun invoke(ref: IndexIterable): IndexIterator {
//            val threadName = currentThreadName
//            var i = iteratorPool.nextIndex(0)
//            while (i >= 0) {
//                val n = iteratorPool[i]!!
//                if (!n.hasNext() && n.threadName == threadName) {
//                    n.reset(ref)
//                    // DEBUG  println("*********** reuse old IndexIterator")
//                    return n
//                }
//                i = iteratorPool.nextIndex(i + 1)
//            }
//            // DEBUG  println("******** create new IndexIterator")
//            val r = IndexIterator(ref)
//            iteratorPool.add(r)
//            return r
//        }
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
            //println("*********** reuse old IndexedTypeIterator ${n.hashCode()} queueSize: ${iteratorPool.size}")
            n.reset(ref)
            return n
        }
//        @Suppress("UNCHECKED_CAST")
//        operator fun <T> invoke(ref: IndexedTypeIterable<T>): IndexedTypeIterator<T> {
//            val threadName = currentThreadName
//            var i = iteratorPool.nextIndex(0)
//            val now = Engine.timer.time
//            while (i >= 0) {
//                val n: IndexedTypeIterator<T> = iteratorPool[i]!! as IndexedTypeIterator<T>
//                if (!n.hasNext() && now - n.lastCall > 2d && n.threadName == threadName) {
//                    n.reset(ref)
//                    //println("*********** reuse old IndexedTypeIterator ${n.hashCode()}")
//                    return n
//                }
//                i = iteratorPool.nextIndex(i + 1)
//            }
//            // DEBUG  println("******** create new IndexedTypeIterator<T>")
//            val r = IndexedTypeIterator(ref)
//            iteratorPool.add(r)
//            return r
//        }
    }
}