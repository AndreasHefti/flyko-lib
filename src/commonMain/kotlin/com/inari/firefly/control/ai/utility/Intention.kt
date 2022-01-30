package com.inari.firefly.control.ai.utility

import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

class Intention private constructor() : SystemComponent(Intention::class.simpleName!!) {

    @JvmField val considerations = BitSet()

    @JvmField val withConsideration = ComponentRefResolver(Consideration) { considerations[it] = true }
    fun <A : Consideration> withConsideration(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        considerations.set(result.index)
        return result
    }
    @JvmField val removeConsideration = ComponentRefResolver(Consideration) { considerations[it] = false }

    fun getUtilityValue(entityId: Int): Float {
        var index = considerations.nextSetBit(0)
        var result = 0f
        while (index >= 0) {
            result += UtilityAISystem.considerations[index].getUtilityValue(entityId, this.index)
            index = considerations.nextSetBit(index)
        }
        return result
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<Intention>(Intention::class) {
        override fun createEmpty() = Intention()
    }
}