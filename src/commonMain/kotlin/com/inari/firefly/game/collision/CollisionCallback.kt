package com.inari.firefly.game.collision

import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.physics.contact.Contacts
import com.inari.util.Predicate
import com.inari.util.aspect.Aspect
import kotlin.jvm.JvmField

data class CollisionCallback(
    @JvmField val material: Aspect = UNDEFINED_MATERIAL,
    @JvmField val contact: Aspect = UNDEFINED_CONTACT_TYPE,
    @JvmField val callback: Predicate<Contacts>
)