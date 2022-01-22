package com.inari.firefly.physics.contact

import com.inari.firefly.core.component.CompId
import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class ContactScans internal constructor() {

    @JvmField internal var scans: DynArray<ContactScan> = DynArray.nullArray()
    val hasAnyScan get() = !scans.isEmpty

    fun registerScan(fullScan: FullContactScan) {
        if (scans == DynArray.NULL_ARRAY)
            scans = DynArray.of()
        scans.set(fullScan.constraintRef, fullScan)
    }

    fun registerScan(simpleScan: SimpleContactScan) {
        if (scans == DynArray.NULL_ARRAY)
            scans = DynArray.of()
        scans.set(simpleScan.constraintRef, simpleScan)
    }

    fun removeScan(index: Int) {
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
            val scan = scans[i++] ?: continue
            if (scan.hasAnyContact())
                return true
        }
        return false
    }

    fun hasAnyContactOfType(contactAspect: Aspect): Boolean {
        var i = 0
        while (i < scans.capacity) {
            val scan = scans[i++] ?: continue
            if (scan.hasContactOfType(contactAspect))
                return true
        }
        return false
    }

    fun getSimpleScan(constraint: ContactConstraint): SimpleContactScan? = getSimpleScan(constraint.index)
    fun getSimpleScan(constraint: CompId): SimpleContactScan? = getSimpleScan(constraint.instanceId)
    fun getSimpleScan(constraintRef: Int): SimpleContactScan? = scans[constraintRef] as SimpleContactScan
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