package com.inari.firefly.physics.contact

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.util.collection.BitSet
import com.inari.util.geom.Rectangle

class SimpleContactMap : ContactMap() {

    private val entities = BitSet(100)
    private val pool = ArrayDeque<EntityIdIterator>()
    init {
        pool.add(EntityIdIterator())
        pool.add(EntityIdIterator())
    }

    override fun add(entity: Entity) =
        entities.set(entity.index)


    override fun remove(entity: Entity) =
        entities.set(entity.index, false)


    override fun update(entity: Entity) {
        // not needed here
    }

    override fun update(entityId: Int, transform: ETransform, collision: EContact) {
        // not needed here
    }

    override fun get(region: Rectangle, entity: Entity): IntIterator {
        if (pool.isEmpty())
            pool.add(EntityIdIterator())

        val iterator = pool.removeFirst()
        iterator.reset(entity.index)
        return iterator
    }

    override fun clear() =
        entities.clear()


    override fun componentType() = Companion
    companion object : SystemComponentSubType<ContactMap, SimpleContactMap>(ContactMap, SimpleContactMap::class) {
        override fun createEmpty() = SimpleContactMap()
    }


    private inner class EntityIdIterator : IntIterator() {

        private var index: Int = -1
        private var exclude: Int = -1

        override fun hasNext(): Boolean =
            index >= 0

        override fun nextInt(): Int {
            val result = index
            findNext()
            return result
        }

        private fun findNext() {
            index = entities.nextSetBit(index + 1)
            if (index == exclude)
                index = entities.nextSetBit(index + 1)

            if (index < 0)
                pool.add(this)
        }

        fun reset(exclude: Int) {
            index = -1
            this.exclude = exclude
            findNext()
        }
    }
}