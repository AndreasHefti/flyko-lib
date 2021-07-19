package com.inari.firefly.physics.contact

import com.inari.firefly.core.component.CompId
import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField


class ContactScan internal constructor() {

    @JvmField internal val contacts: DynArray<Contacts> = DynArray.of()

    fun hasAnyContact(): Boolean {
        var i = 0
        while (i < contacts.capacity) {
            val c = contacts[i++] ?: continue
            if (c.hasAnyContact())
                return true
        }
        return false
    }

    fun hasContact(contact: Aspect): Boolean {
        var i = 0
        while (i < contacts.capacity) {
            val c = contacts[i++] ?: continue
            if (c.hasContact(contact))
                return true
        }
        return false
    }

    operator fun get(constraint: ContactConstraint): Contacts =
        get(constraint.index)

    operator fun get(constraint: CompId): Contacts =
        get(constraint.instanceId)

    operator fun get(constraint: Int): Contacts =
        contacts[constraint]!!

    fun clearContacts() {
        var i = 0
        while (i < contacts.capacity)
            contacts[i++]?.clear()
    }

    internal fun clear() {
        clearContacts()
        contacts.clear()
    }
}