package com.inari.firefly.control

import com.inari.firefly.control.behavior.EBehavior
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


typealias Operation<C> = (C) -> OpResult
typealias UpdateOperation = () -> OpResult
typealias TaskOperation = () -> OpResult
typealias TaskCallback = (OpResult) -> Any
typealias ComponentTaskOperation = (CompId, CompId, CompId) -> OpResult
typealias IntOperation = (Int) -> OpResult
typealias ComponentIdOperation = (CompId) -> OpResult

@JvmField val EMPTY_INT_OPERATION: IntOperation = { OpResult.SUCCESS  }
@JvmField val EMPTY_TASK_OPERATION: TaskOperation =  { OpResult.SUCCESS }
@JvmField val EMPTY_COMPONENT_TASK_OPERATION: ComponentTaskOperation = { _, _, _  -> OpResult.SUCCESS }


/** A condition that takes an entity id as input */
typealias EntityCondition = (Int) -> Boolean
fun EntityCondition.and(otherCondition: EntityCondition): EntityCondition = { id ->
    this(id) && otherCondition(id)
}

/** An ActionCall takes some int identifiers (usually the entityId) and executes the concrete action */
typealias EntityActionCall = (Int, Int, Int, Int) -> Unit
@JvmField val EMPTY_ENTITY_ACTION_CALL: EntityActionCall = { _, _, _, _ -> }
/** A Consideration takes an entity id and returns a normalized float value (0 - 1) */
typealias Consideration = (Int) -> Float

typealias BxOp = (Entity, EBehavior) -> OpResult
typealias BxConditionOp = (Entity, EBehavior) -> Boolean

@JvmField val BEHAVIOR_STATE_ASPECT_GROUP = IndexedAspectType("BEHAVIOR_STATE_ASPECT_GROUP")
@JvmField val UNDEFINED_BEHAVIOR_STATE: Aspect = BEHAVIOR_STATE_ASPECT_GROUP.createAspect("UNDEFINED_BEHAVIOR_STATE")

@JvmField val TRUE_CONDITION: BxConditionOp = { _, _ -> true }
@JvmField val FALSE_CONDITION: BxConditionOp = { _, _ -> false }
@JvmField val NOT: (BxConditionOp) -> BxConditionOp = {
        c -> { entity, bx -> ! c(entity, bx) }
}
@JvmField val AND: (BxConditionOp, BxConditionOp) -> BxConditionOp = {
        c1, c2 -> { entity, bx -> c1(entity, bx) && c2(entity, bx) }
}
@JvmField val OR: (BxConditionOp, BxConditionOp) -> BxConditionOp = {
        c1, c2 -> { entity, bx -> c1(entity, bx) || c2(entity, bx) }
}
@JvmField val ACTION_DONE_CONDITION =  { aspect: Aspect -> {
        _ : Entity, behavior: EBehavior -> aspect in behavior.actionsDone }
}
@JvmField val SUCCESS_ACTION: BxOp = { _, _ -> OpResult.SUCCESS }
@JvmField val FAIL_ACTION: BxOp = { _, _ -> OpResult.FAILED }