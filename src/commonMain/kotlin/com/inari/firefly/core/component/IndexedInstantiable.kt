package com.inari.firefly.core.component

interface IndexedInstantiable {
    val instanceId: Int
}

interface IndexedInstantiableList : IndexedInstantiable {
    fun instanceId(index: Int): Int
}