package com.inari.firefly.game

import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.physics.contact.Contacts

/** { entityId, velocity, button -> } */
typealias MoveCallback = (Int, Float, ButtonType) -> Unit
val VOID_MOVE_CALLBACK: MoveCallback = { _, _, _ -> }

/** { entityId, cardinality, contacts -> } */
typealias OnSlopeCallback = (Int, Int, Contacts) -> Unit
val VOID_ON_SLOPE_CALLBACK: OnSlopeCallback = { _, _, _ -> }

/** { entityId -> } */
typealias TouchGroundCallback = (Int) -> Unit
val VOID_TOUCH_GROUND_CALLBACK: TouchGroundCallback = { _ -> }

/** { entityId -> } */
typealias LooseGroundContactCallback = (Int) -> Unit
val VOID_LOOS_GROUND_CALLBACK: TouchGroundCallback = { _ -> }