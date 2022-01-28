package com.inari.firefly.control

import com.inari.firefly.core.component.CompId
import kotlin.jvm.JvmField

enum class OpResult {
    SUCCESS,
    RUNNING,
    FAILED
}

typealias ValueOperation<C> = (C) -> OpResult
typealias UpdateOperation = () -> OpResult

/** An action that can have up to three component identifiers as input.
 *  Usually the first component input is the actual entity id and the other are defined by a
 *  concrete EntityAction implementation */
typealias EntityActionCall = (Int, Int, Int) -> OpResult
operator fun EntityActionCall.invoke() = this(-1, -1, -1)
operator fun EntityActionCall.invoke(entityId: Int) = this(entityId, -1, -1)
operator fun EntityActionCall.invoke(entityId1: Int, entityId2: Int) = this(entityId1, entityId2, -1)

/** A condition that takes an entity id as an input */
typealias EntityCondition = (Int) -> Boolean
fun EntityCondition.and(otherCondition: EntityCondition): EntityCondition = { id ->
    this(id) && otherCondition(id)
}
fun EntityCondition.or(otherCondition: EntityCondition): EntityCondition = { id ->
    this(id) || otherCondition(id)
}

typealias TaskCallback = (CompId, OpResult) -> Any
@JvmField val EMPTY_TASK_CALLBACK: TaskCallback = { _,_ -> }

@JvmField val EMPTY_UPDATE_OPERATION: UpdateOperation = { OpResult.SUCCESS }
@JvmField val EMPTY_ENTITY_ACTION_CALL: EntityActionCall = { _, _, _ -> OpResult.SUCCESS }
@JvmField val TRUE_ENTITY_CONDITION: EntityCondition = { true }
@JvmField val FALSE_ENTITY_CONDITION: EntityCondition = { true }


