package com.inari.firefly.physics.contact

import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynIntArray
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField
import kotlin.math.floor
import com.inari.util.IntIterator
import com.inari.util.geom.Vector3i

class SimpleContactScan  internal constructor(
    @JvmField internal var constraintRef: Int
) : ContactScan {

    override val constraint = ContactSystem.constraints[constraintRef]
    @JvmField internal val normalizedContactBounds = Vector4i()
    @JvmField internal val worldBounds = Vector4i()
    @JvmField internal val entities = DynIntArray(1, -1, 1)

    override fun hasAnyContact() = !entities.isEmpty

    override fun hasContactOfType(type: Aspect): Boolean {
        TODO("Not yet implemented")
    }

    fun getContactEntityIterator(): IntIterator = null!!

    private val otherWorldBounds = Vector4i()
    override fun scanFullContact(
        worldBounds: Vector4i,
        otherEntity: Entity,
        otherWorldPos: Vector2f
    ) {
        val otherContact = otherEntity[EContact]
        if (!constraint.match(otherContact))
            return

        otherWorldBounds(
            (floor(otherWorldPos.x.toDouble()) + otherContact.bounds.x).toInt(),
            (floor(otherWorldPos.x.toDouble()) + otherContact.bounds.x).toInt())
        if (constraint.isCircle) {
            if (otherContact.isCircle) {
                // both are circles
                otherWorldBounds.radius = otherContact.bounds.radius
                if (GeomUtils.intersectCircle(worldBounds as Vector3i, otherWorldBounds as Vector3i))
                    entities.add(otherEntity.index)
            } else {
                otherWorldBounds.width = otherContact.bounds.width
                otherWorldBounds.height = otherContact.bounds.height
                if (GeomUtils.intersectCircle(worldBounds as Vector3i, otherWorldBounds))
                    entities.add(otherEntity.index)
            }

        } else {
            if (otherContact.isCircle) {
                otherWorldBounds.radius = otherContact.bounds.radius
                if (GeomUtils.intersectCircle(worldBounds as Vector3i, otherWorldBounds))
                    entities.add(otherEntity.index)
            } else {
                otherWorldBounds.width = otherContact.bounds.width
                otherWorldBounds.height = otherContact.bounds.height
                if (GeomUtils.intersect(worldBounds, otherWorldBounds))
                    entities.add(otherEntity.index)
            }
        }
    }

    override fun clear() {
        entities.clear()
    }
}