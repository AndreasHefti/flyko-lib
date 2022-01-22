package com.inari.firefly.physics.contact

import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynIntArray
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField
import com.inari.util.IntIterator

class SimpleContactScan  internal constructor(
    @JvmField internal var constraintRef: Int
) : ContactScan {

    override val constraint = ContactSystem.constraints[constraintRef]
    @JvmField internal val normalizedContactBounds = Vector4i()
    @JvmField internal val entities = DynIntArray(1, -1, 1)

    override fun hasAnyContact() = !entities.isEmpty

    override fun hasContactOfType(type: Aspect): Boolean {
        TODO("Not yet implemented")
    }
    fun getContactEntityIterator(): IntIterator = null!!

    override fun scanFullContact(
        originWorldContact: ContactBounds,
        otherWorldContact: ContactBounds,
        otherContactDef: EContact,
        otherEntityId: Int
    ) {
        if (!constraint.match(otherContactDef))
            return

// TODO this should be done by the System
//        otherWorldBounds(
//            (floor(otherWorldPos.x.toDouble()) + otherContact.bounds.x).toInt(),
//            (floor(otherWorldPos.x.toDouble()) + otherContact.bounds.x).toInt())

        if (scanContact(originWorldContact,otherWorldContact ))
            entities.add(otherEntityId)
    }

    override fun clear() {
        entities.clear()
    }

    companion object {
        fun scanContact(originWorldContact: ContactBounds, otherWorldContact: ContactBounds): Boolean =
            if (originWorldContact.isCircle) {
                if (otherWorldContact.isCircle)
                    // both are circles
                    (GeomUtils.intersectCircle(originWorldContact.circle!!, otherWorldContact.circle!!))
                else
                    (GeomUtils.intersectCircle(originWorldContact.circle!!, otherWorldContact.bounds))
            } else {
                if (otherWorldContact.isCircle)
                    (GeomUtils.intersectCircle(otherWorldContact.circle!!, originWorldContact.bounds))
                else
                    (GeomUtils.intersect(originWorldContact.bounds, otherWorldContact.bounds))
            }
        }
}