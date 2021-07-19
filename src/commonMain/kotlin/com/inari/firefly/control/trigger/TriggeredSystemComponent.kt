package com.inari.firefly.control.trigger

import com.inari.firefly.core.system.SystemComponent
import com.inari.util.Call
import com.inari.util.collection.BitSet

abstract class TriggeredSystemComponent protected constructor(
    objectIndexerName: String
) : SystemComponent(objectIndexerName) {

    private val trigger = BitSet()

    protected fun <A : Trigger> trigger(cBuilder: Trigger.Subtype<A>, call: Call, configure: (A.() -> Unit)): A {
        val trigger = cBuilder.doBuild(configure)
        TriggerSystem.trigger.receiver()(trigger)
        trigger.register(call)
        return trigger
    }

    override fun dispose() {
        var i = trigger.nextSetBit(0)
        while (i >= 0) {
            TriggerSystem.trigger.delete(i)
            i = trigger.nextSetBit(i + 1)
        }
        super.dispose()
    }
}