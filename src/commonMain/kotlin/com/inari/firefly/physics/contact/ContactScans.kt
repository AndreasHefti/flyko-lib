package com.inari.firefly.physics.contact

import com.inari.firefly.core.component.CompId
import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class ContactScans internal constructor() {

    @JvmField internal var scans: DynArray<FullContactScan> = DynArray.nullArray()
    val hasAnyScan get() = !scans.isEmpty

    fun registerFullScan(contacts: FullContactScan) {
        if (scans == DynArray.NULL_ARRAY)
            scans = DynArray.of()
        scans.set(contacts.constraintRef, contacts)
    }

    fun removeFullScan(index: Int) {
        if (scans == DynArray.NULL_ARRAY)
            return;
        scans.remove(index)
        if (scans.isEmpty)
            scans = DynArray.nullArray()
    }

    fun hasAnyContactForConstraint(constraintId: Int): Boolean {
        return scans[constraintId]?.hasAnyContact() ?: false
    }

    fun hasAnyContact(): Boolean {
        var i = 0
        while (i < scans.capacity) {
            val fullScan = scans[i++] ?: continue
            if (fullScan.hasAnyContact())
                return true
        }
        return false
    }

    fun hasAnyContactOfType(contactAspect: Aspect): Boolean {
        var i = 0
        while (i < scans.capacity) {
            val fullScan = scans[i++] ?: continue
            if (fullScan.hasContactOfType(contactAspect))
                return true
        }
        return false
    }

    fun getFullScan(constraint: ContactConstraint): FullContactScan? = getFullScan(constraint.index)
    fun getFullScan(constraint: CompId): FullContactScan? = getFullScan(constraint.instanceId)
    fun getFullScan(constraintRef: Int): FullContactScan? = scans[constraintRef] as FullContactScan

    fun clearContacts() {
        var i = 0
        while (i < scans.capacity)
            scans[i++]?.clear()
    }

    internal fun clear() {
        clearContacts()
        scans.clear()
    }
}