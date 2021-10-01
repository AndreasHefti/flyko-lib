package com.inari.firefly.game

import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.physics.contact.Contacts

typealias MoveCallback = (Int, Float, ButtonType) -> Unit
val VOID_MOVE_CALLBACK: MoveCallback = { entityId, velocity, button -> }

typealias OnSlopeCallback = (Int, Int, Contacts) -> Unit
val VOID_ON_SLOPE_CALLBACK: OnSlopeCallback = { entityId, cardinality, contacts -> }

typealias TouchGroundCallback = (Int) -> Unit
val VOID_TOUCH_GROUND_CALLBACK: TouchGroundCallback = { entityId -> }

typealias LooseGroundContactCallback = (Int) -> Unit
val VOID_LOOS_GROUND_CALLBACK: TouchGroundCallback = { entityId -> }