package com.inari.firefly.composite

import kotlin.jvm.JvmField

abstract class AttributedComposite : Composite() {

     @JvmField protected val attributes: MutableMap<String,String> = mutableMapOf()

    operator fun set(name: String, value: String) {
        attributes[name] = value
    }

    fun addAttributes(attributes: Map<String,String>) {
        this.attributes + attributes
    }

}