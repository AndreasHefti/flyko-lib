package com.inari.firefly.core.component

import com.inari.util.Consumer
import com.inari.util.collection.DynIntArrayRO

interface ComponentMapRO<C : Component> {

    val isEmpty: Boolean
    operator fun contains(index: Int): Boolean
    operator fun contains(name: String): Boolean
    operator fun contains(id: CompId): Boolean
    fun idForName(name: String): CompId
    fun indexForName(name: String): Int
    fun isActive(index: Int): Boolean
    fun isActive(name: String): Boolean
    fun isActive(id: CompId): Boolean
    operator fun get(index: Int): C
    operator fun get(name: String): C
    operator fun get(id: CompId): C
    fun getActive(index: Int): C? = if (isActive(index)) get(index) else null
    fun getActive(name: String): C? = if (isActive(name)) get(name) else null
    fun getActive(id: CompId): C? = if (isActive(id)) get(id) else null
    fun <CC : C> getAs(index: Int): CC
    fun <CC : C> getAs(name: String): CC
    fun <CC : C> getAs(id: CompId): CC
    fun forEach(expr: Consumer<C>)
    fun forEachActive(expr: Consumer<C>)
    fun forEachIn(bag: DynIntArrayRO, expr: Consumer<C>)
    fun <CC : C> forEachSubtypeIn(bag: DynIntArrayRO, expr: Consumer<CC>)


}