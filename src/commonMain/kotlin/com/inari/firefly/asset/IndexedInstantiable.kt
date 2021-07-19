package com.inari.firefly.asset

interface IndexedInstantiable {
    val instanceId: Int
}

interface IndexedInstantiableList : IndexedInstantiable {
    fun instanceId(index: Int): Int
}