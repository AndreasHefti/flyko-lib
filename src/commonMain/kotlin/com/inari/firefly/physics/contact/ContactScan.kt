package com.inari.firefly.physics.contact

import com.inari.util.aspect.Aspect

interface ContactScan {

    val constraint: ContactConstraint
    fun hasAnyContact(): Boolean
    fun hasContactOfType(type: Aspect): Boolean
    fun scanFullContact(
        originWorldContact: ContactBounds,
        otherWorldContact: ContactBounds,
        otherContactDef: EContact,
        otherEntityId: Int)

    fun clear()
}