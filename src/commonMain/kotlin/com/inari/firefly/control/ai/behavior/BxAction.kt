package com.inari.firefly.control.ai.behavior


import com.inari.firefly.control.EMPTY_ENTITY_ACTION_CALL
import com.inari.firefly.control.EntityActionCall
import com.inari.firefly.control.OpResult
import com.inari.firefly.control.action.EntityAction
import com.inari.firefly.control.action.EntityActionSystem
import com.inari.firefly.control.invoke
import com.inari.firefly.control.task.Task
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import kotlin.jvm.JvmField

class BxAction private constructor() : BxNode() {

    @JvmField internal var actionRef: EntityActionCall = EMPTY_ENTITY_ACTION_CALL
    @JvmField val action = ComponentRefResolver(EntityAction) { actionRef = EntityActionSystem.actions[it].call }
    fun <A : EntityAction> withEntityAction(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        actionRef = result.call
        return result.componentId
    }
    fun withEntityAction(actionCall: EntityActionCall) {
        val result = EntityAction.buildAndGet {
            name = super.name
            call = actionCall
        }
        actionRef = result.call
    }
    override fun tick(entityId: Int): OpResult = actionRef(entityId)

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxAction>(BxNode, BxAction::class) {
        override fun createEmpty() = BxAction()
    }

}