package com.inari.firefly.control.state

import com.inari.firefly.FALSE_SUPPLIER
import com.inari.firefly.NO_NAME
import com.inari.firefly.control.task.Task
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.util.BooleanSupplier
import kotlin.jvm.JvmField

@ComponentDSL
class StateChange internal constructor() {

    var name: String = NO_NAME
        set(value) {
            if (name !== NO_NAME)
                throw IllegalStateException("An illegal reassignment of name: $value to: $name")
            field = value
        }

    @JvmField var fromState: String = NO_NAME
    @JvmField internal var disposeStateTaskRef: Int = -1
    @JvmField var toState: String = NO_NAME
    @JvmField internal var initStateTaskRef: Int = -1
    @JvmField var condition: BooleanSupplier = FALSE_SUPPLIER


    val withDisposeStateTask = ComponentRefResolver(Task) { index -> disposeStateTaskRef = index }
    fun <A : Task> withDisposeStateTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        disposeStateTaskRef = result.index
        return result.componentId
    }

    val withInitStateTask = ComponentRefResolver(Task) { index -> initStateTaskRef = index }
    fun <A : Task> withInitStateTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        initStateTaskRef = result.index
        return result.componentId
    }

}