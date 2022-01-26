package com.inari.firefly.physics.contact

import com.inari.util.aspect.Aspect

abstract class ContactScan {

    abstract val constraint: ContactConstraint
    abstract fun hasAnyContact(): Boolean
    abstract fun hasContactOfType(type: Aspect): Boolean
    internal abstract fun scanFullContact(
        originWorldContact: SystemContactBounds,
        otherWorldContact: SystemContactBounds,
        otherContactDef: EContact,
        otherEntityId: Int)

    abstract fun clear()
}