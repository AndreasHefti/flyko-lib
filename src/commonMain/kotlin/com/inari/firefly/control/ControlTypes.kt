package com.inari.firefly.control

import com.inari.firefly.control.ai.behavior.EBehavior
import com.inari.firefly.core.component.CompId
import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import kotlin.jvm.JvmField

enum class OpResult {
    SUCCESS,
    RUNNING,
    FAILED
}

typealias EntityAction = (Int, Int, Int, Int) -> OpResult

typealias Operation<C> = (C) -> OpResult
typealias UpdateOperation = () -> OpResult
typealias TaskOperation = () -> OpResult
typealias TaskCallback = (OpResult) -> Any
typealias ComponentTaskOperation = (CompId, CompId, CompId) -> OpResult
typealias IntOperation = (Int) -> OpResult
typealias ComponentIdOperation = (CompId) -> OpResult

@JvmField val EMPTY_ENTITY_ACTION_CALL: EntityAction = { _,_,_,_ -> OpResult.SUCCESS }
@JvmField val EMPTY_INT_OPERATION: IntOperation = { OpResult.SUCCESS  }
@JvmField val EMPTY_TASK_OPERATION: TaskOperation =  { OpResult.SUCCESS }
@JvmField val EMPTY_COMPONENT_TASK_OPERATION: ComponentTaskOperation = { _, _, _  -> OpResult.SUCCESS }

/** A condition that takes an entity id as an input */
typealias EntityCondition = (Int) -> Boolean
fun EntityCondition.and(otherCondition: EntityCondition): EntityCondition = { id ->
    this(id) && otherCondition(id)
}
fun EntityCondition.or(otherCondition: EntityCondition): EntityCondition = { id ->
    this(id) || otherCondition(id)
}

typealias BxOp = (Entity, EBehavior) -> OpResult
typealias BxConditionOp = (Entity, EBehavior) -> Boolean
fun BxConditionOp.and(otherCondition: BxConditionOp): BxConditionOp = {
        entity, bx -> this(entity, bx) && otherCondition(entity, bx)
}
fun BxConditionOp.or(otherCondition: BxConditionOp): BxConditionOp = {
        entity, bx -> this(entity, bx) && otherCondition(entity, bx)
}
fun BxConditionOp.not(): BxConditionOp = {
        entity, bx -> !this(entity, bx)
}

@JvmField val BEHAVIOR_STATE_ASPECT_GROUP = IndexedAspectType("BEHAVIOR_STATE_ASPECT_GROUP")
@JvmField val UNDEFINED_BEHAVIOR_STATE: Aspect = BEHAVIOR_STATE_ASPECT_GROUP.createAspect("UNDEFINED_BEHAVIOR_STATE")

@JvmField val TRUE_BX_CONDITION: BxConditionOp = { _, _ -> true }
@JvmField val FALSE_BX_CONDITION: BxConditionOp = { _, _ -> false }
@JvmField val SUCCESS_BX_OPERATION: BxOp = { _, _ -> OpResult.SUCCESS }
@JvmField val FAIL_BX_OPERATION: BxOp = { _, _ -> OpResult.FAILED }