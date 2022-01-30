package com.inari.firefly.control

import kotlin.jvm.JvmField

enum class OpResult {
    SUCCESS,
    RUNNING,
    FAILED
}

//typealias ValueOperation<C> = (C) -> OpResult
typealias TaskCallback = (Int, OpResult) -> Any
typealias UpdateOperation = () -> OpResult

/** A function that can have up to three component identifiers as input.
 *  Usually the first component input is the actual entity id and the other are defined by a
 *  concrete EntityAction implementation */
typealias ActionOperation = (Int, Int, Int) -> OpResult
operator fun ActionOperation.invoke() = this(-1, -1, -1)
operator fun ActionOperation.invoke(entityId: Int) = this(entityId, -1, -1)
operator fun ActionOperation.invoke(entityId1: Int, entityId2: Int) = this(entityId1, entityId2, -1)

typealias NormalOperation = (Int, Int, Int) -> Float
operator fun NormalOperation.invoke() = this(-1, -1, -1)
operator fun NormalOperation.invoke(entityId: Int) = this(entityId, -1, -1)
operator fun NormalOperation.invoke(entityId1: Int, entityId2: Int) = this(entityId1, entityId2, -1)

typealias ConditionOperation = (Int, Int, Int) -> Boolean
operator fun ConditionOperation.invoke() = this(-1, -1, -1)
operator fun ConditionOperation.invoke(entityId: Int) = this(entityId, -1, -1)
operator fun ConditionOperation.invoke(entityId1: Int, entityId2: Int) = this(entityId1, entityId2, -1)

@JvmField val EMPTY_TASK_CALLBACK: TaskCallback = { _,_ -> }
@JvmField val EMPTY_UPDATE_OP: UpdateOperation = { OpResult.SUCCESS }
@JvmField val EMPTY_OP: ActionOperation = { _, _, _ -> OpResult.SUCCESS }
@JvmField val ZERO_OP: NormalOperation = { _, _, _ -> 0f }
@JvmField val ONE_OP : NormalOperation = { _, _, _ -> 1f }
@JvmField val TRUE_OP: ConditionOperation = { _, _, _ -> true }
@JvmField val FALSE_OP : ConditionOperation = { _, _, _ -> false }

///** A condition that takes an entity id as an input */
//typealias EntityCondition = (Int) -> Boolean
//fun EntityCondition.and(otherCondition: EntityCondition): EntityCondition = { id ->
//    this(id) && otherCondition(id)
//}
//fun EntityCondition.or(otherCondition: EntityCondition): EntityCondition = { id ->
//    this(id) || otherCondition(id)
//}
//

//@JvmField val EMPTY_TASK_CALLBACK: TaskCallback = { _,_ -> }
//
//@JvmField val EMPTY_UPDATE_OPERATION: UpdateOperation = { OpResult.SUCCESS }
//@JvmField val EMPTY_ENTITY_ACTION_CALL: OperationFunction = { _, _, _ -> OpResult.SUCCESS }
//@JvmField val TRUE_ENTITY_CONDITION: EntityCondition = { true }
//@JvmField val FALSE_ENTITY_CONDITION: EntityCondition = { true }


