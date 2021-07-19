package com.inari.firefly.entity

import com.inari.firefly.core.ComponentRefResolver
import kotlin.jvm.JvmField


class EChild private constructor () : EntityComponent(EChild::class.simpleName!!) {

    @JvmField internal var int_parent: Int = -1

    var parent = ComponentRefResolver(Entity) { index -> int_parent = index }
    var zPos: Int = -1

    override fun reset() {
        int_parent = -1
        zPos = -1
    }

    override fun componentType() =  Companion
    companion object : EntityComponentType<EChild>(EChild::class) {
        override fun createEmpty() = EChild()
    }
}