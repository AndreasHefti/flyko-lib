package com.inari.firefly.physics.contact

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.view.*
import com.inari.firefly.physics.movement.MovementSystem
import com.inari.util.collection.BitSet
import com.inari.util.collection.IndexIterator
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

abstract class ContactMap protected constructor() : Component(ContactMap), ViewLayerAware  {

    override val viewIndex: ComponentIndex
        get() = viewRef.targetKey.componentIndex
    override val layerIndex: ComponentIndex
        get() = layerRef.targetKey.componentIndex
    @JvmField val viewRef = CReference(View)
    @JvmField val layerRef = CReference(Layer)

    protected val entities: BitSet = BitSet()

    internal fun notifyEntityActivation (index: EntityIndex) {
        val transform = ETransform[index]
        if (viewIndex != transform.viewIndex || layerIndex != transform.layerIndex)
            return

        entities[index] = true
    }

    internal fun notifyEntityDeactivation(index: EntityIndex) {
        entities[index] = false
    }

    private val tempBitset = BitSet()
    fun updateAll(entities: BitSet) {
        tempBitset.clear()
        tempBitset.and(entities)
        tempBitset.and(this.entities)

        var index = tempBitset.nextSetBit(0)
        while (index >= 0) {
            update(index)
            index = tempBitset.nextSetBit(index + 1)
        }
    }

    /** This is usually called by CollisionSystem in an entity move event and must update the entity in the pool
     * if the entity id has some orientation related store attributes within the specified ContactPool implementation.
     *
     * @param entityIndex the index of an entity that has just moved and changed its position in the world
     */
    abstract fun update(entityIndex: EntityIndex)

    /** Use this to get an IntIterator of all entity id's that most possibly has a collision within the given region.
     * The efficiency of this depends on an specified implementation and can be different for different needs.
     *
     * @param region The contact or collision region to check collision entity collisions against.
     * @param entityIndex Entity index of the Entity to exclude from the search
     * @return IndexIterator of all entity id's that most possibly has a collision within the given region
     */
    abstract operator fun get(region: Vector4i, entityIndex: EntityIndex): IndexIterator

    companion object : ComponentSystem<ContactMap>("ContactMap") {

        val VIEW_LAYER_MAPPING = ViewLayerMapping()

        override fun registerComponent(c: ContactMap): ComponentKey {
            val key = super.registerComponent(c)
            VIEW_LAYER_MAPPING.add(c, c.index)
            return key
        }

        override fun unregisterComponent(index: ComponentIndex) {
            val c = this[index]
            VIEW_LAYER_MAPPING.delete(c, c.index)
            super.unregisterComponent(index)
        }

        private val entityListener: ComponentEventListener = { key, type ->
            val index = key.componentIndex
            if (index in EContact && index !in ETile) {
                if (type == ComponentEventType.ACTIVATED) {
                    val iter = iterator()
                    while (iter.hasNext())
                        iter.next().notifyEntityActivation(index)
                } else if (type == ComponentEventType.DEACTIVATED) {
                    val iter = iterator()
                    while (iter.hasNext())
                        iter.next().notifyEntityDeactivation(key.componentIndex)
                }
            }
        }

        private val viewListener: ComponentEventListener = { key, type ->
            if (type == ComponentEventType.DELETED) {
                val iter = iterator()
                while (iter.hasNext())
                    if (iter.next().viewIndex == key.componentIndex)
                        delete(key.componentIndex)
            }
        }

        private val moveListener: (MovementSystem.MoveEvent) -> Unit = {
            var index =  activeComponentSet.nextIndex(0)
            while (index >= 0) {
                ContactMap[index].updateAll(it.entities)
                index = activeComponentSet.nextIndex(index + 1)
            }
        }

        init {
            Entity.registerComponentListener(entityListener)
            View.registerComponentListener(viewListener)
            Engine.registerListener(MovementSystem.moveEvent, moveListener)
        }

        fun update(entityIndex: EntityIndex) {
            val maps = VIEW_LAYER_MAPPING[ETransform[entityIndex]]
            if (maps.isEmpty) return

            var index = maps.nextSetBit(0)
            while (index >= 0) {
                ContactMap[index].update(index)
                index = maps.nextSetBit(index + 1)
            }
        }

        override fun allocateArray(size: Int): Array<ContactMap?> = arrayOfNulls(size)
        override fun create(): ContactMap =
            throw UnsupportedOperationException("ContactMap is abstract use a concrete implementation instead")
    }
}

class SimpleContactMap private constructor(): ContactMap() {

    override fun update(entityIndex: EntityIndex) {
        // not needed here since this is just an ordinary list
    }

    private val selfExcluded = BitSet()
    override fun get(region: Vector4i, entityIndex: EntityIndex): IndexIterator {
        selfExcluded.clear()
        selfExcluded.or(entities)
        selfExcluded[entityIndex] = false
        return IndexIterator(selfExcluded)
    }

    companion object : SubComponentBuilder<ContactMap, SimpleContactMap>(ContactMap) {
        override fun create() = SimpleContactMap()
    }
}