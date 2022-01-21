package com.inari.firefly.physics.contact

import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspect
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4i

interface ContactScan {

    val constraint: ContactConstraint
    val hasAnyContact: Boolean

    //fun getContactEntityIterator() : IntIterator
    fun hasContactType(contactTypeAspect: Aspect): Boolean

    fun scanFullContact(worldBounds: Vector4i, normalizedContactBounds: Vector4i, otherEntity: Entity, otherWorldPos: Vector2f)
    fun clear()

}