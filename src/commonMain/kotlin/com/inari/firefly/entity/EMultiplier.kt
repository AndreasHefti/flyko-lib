package com.inari.firefly.entity

import com.inari.util.collection.DynFloatArray
import kotlin.jvm.JvmField

class EMultiplier private constructor () : EntityComponent(EMultiplier::class.simpleName!!) {

    @JvmField var positions: DynFloatArray = DynFloatArray()

    override fun reset() {
        positions.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EMultiplier>(EMultiplier::class) {
        override fun createEmpty() = EMultiplier()
    }
}