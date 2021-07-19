package com.inari.firefly.control.task

import com.inari.firefly.EMPTY_INT_OPERATION
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.IntOperation

class EntityTask private constructor() : TriggeredSystemComponent(EntityTask::class.simpleName!!) {

    /** The task to run as IntOperation
     *  <p>
     *  An IntOperation is defined within an interface so one can be added as an already existing instance that
     *  implements this interface or with an anonymous class implementation in the form:
     *  Task = object : IntOperation { ... }
     */
    var task: IntOperation = EMPTY_INT_OPERATION
        set(value) { field = setIfNotInitialized(value, "task") }

    fun <A : Trigger> trigger(cBuilder: Trigger.Subtype<A>, entityId: CompId, configure: (A.() -> Unit)): A =
            super.trigger(cBuilder, { task(entityId.instanceId) }, configure)

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<EntityTask>(EntityTask::class) {
        override fun createEmpty() = EntityTask()
    }
}