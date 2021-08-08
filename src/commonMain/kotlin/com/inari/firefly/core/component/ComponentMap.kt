package com.inari.firefly.core.component

import com.inari.util.Consumer
import com.inari.util.IntFunction
import com.inari.util.Predicate
import com.inari.util.Receiver
import com.inari.util.collection.DynArrayRO
import com.inari.util.collection.DynIntArrayRO

interface ComponentMap<C : Component> : ComponentMapRO<C> {

    enum class MapAction {
        CREATED, ACTIVATED, DEACTIVATED, DELETED
    }

    val componentType: ComponentType<C>
    val typeIndex: Int
    val map: DynArrayRO<C>
    val activationMapping: Boolean
    val nameMapping: Boolean
    val size: Int

    fun activate(index: Int)
    fun deactivate(index: Int)
    fun activate(name: String)
    fun deactivate(name: String)
    fun activate(id: CompId)
    fun deactivate(id: CompId)
    fun delete(index: Int)
    fun delete(name: String)
    fun delete(id: CompId)
    fun deleteAll(predicate: Predicate<C?>)
    fun indexIterator(predicate: Predicate<C?>): IntFunction
    fun nextActive(from: Int): Int
    fun receiver(): Receiver<C>

    fun clear()
}