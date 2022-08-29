package com.inari.firefly.game.composite

import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.game.EActor
import kotlin.jvm.JvmField

class EComposite private constructor() : EntityComponent(EComposite) {

    @JvmField internal val attributes = mutableMapOf<String, String>()
    fun getAttribute(name: String): String? = attributes[name]
    fun setAttribute(name: String, value: String) { attributes[name] = value }

    override fun reset() = attributes.clear()

    override val componentType = EActor
    companion object : EntityComponentBuilder<EComposite>("EComposite") {
        override fun create() = EComposite()
    }
}