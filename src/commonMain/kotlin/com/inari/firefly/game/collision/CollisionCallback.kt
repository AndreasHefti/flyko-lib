package com.inari.firefly.game.collision

import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.physics.contact.Contacts
import com.inari.util.Predicate
import com.inari.util.aspect.Aspect
import kotlin.jvm.JvmField

/** { entityId, cardinality, contacts -> } */
typealias OnSlopeCallback = (Int, Int, Contacts) -> Unit
val VOID_ON_SLOPE_CALLBACK: OnSlopeCallback = { _, _, _ -> }

/** { entityId -> } */
typealias TouchGroundCallback = (Int) -> Unit
val VOID_TOUCH_GROUND_CALLBACK: TouchGroundCallback = { _ -> }

/** { entityId -> } */
typealias LooseGroundContactCallback = (Int) -> Unit
val VOID_LOOS_GROUND_CALLBACK: TouchGroundCallback = { _ -> }

data class CollisionCallback(
    @JvmField val material: Aspect = UNDEFINED_MATERIAL,
    @JvmField val contact: Aspect = UNDEFINED_CONTACT_TYPE,
    @JvmField val callback: Predicate<Contacts>
)