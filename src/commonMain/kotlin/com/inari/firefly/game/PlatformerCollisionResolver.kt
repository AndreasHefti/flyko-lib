package com.inari.firefly.game

import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.ComponentSubTypeSystem
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.contact.*
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_MATERIAL
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.VOID_INT_CONSUMER
import com.inari.util.ZERO_FLOAT
import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynArray
import com.inari.util.geom.BitMask
import kotlin.jvm.JvmField
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class PlatformerCollisionResolver : CollisionResolver() {

    private var gapSouth = 5
    private var scanLength = 5
    private var bScanLength = gapSouth + scanLength

    private var x1 = 2
    private var x2 = -1
    private var x3 = -1
    private var y1 = 2
    private var y2 = -1
    private var y3 = -1

    private var tmax = -1
    private var bmax = -1
    private var lmax = -1
    private var rmax = -1

    private val contactSensorT1 = BitMask()
    private val contactSensorT2 = BitMask()
    private val contactSensorT3 = BitMask()

    private val contactSensorB1 = BitMask()
    private val contactSensorB2 = BitMask()
    private val contactSensorB3 = BitMask()

    private val contactSensorL1 = BitMask()
    private val contactSensorL2 = BitMask()
    private val contactSensorL3 = BitMask()

    private val contactSensorR1 = BitMask()
    private val contactSensorR2 = BitMask()
    private val contactSensorR3 = BitMask()

    private val contactSensorGround = BitMask()

    private var fullContactConstraintIndex = -1
    private val fullContactCallbacks = DynArray.of<CollisionCallback>()
    private var terrainContactConstraintIndex = -1

    @JvmField var groundContactOffset = 2
    @JvmField var touchGroundCallback: (Int) -> Unit = VOID_INT_CONSUMER
    @JvmField var looseGroundContactCallback: (Int) -> Unit = VOID_INT_CONSUMER
    @JvmField var onSlopeCallback: (Int, Int, FullContactScan) -> Unit = VOID_ON_SLOPE_CALLBACK

    fun withFullContactCallback(
        material: Aspect = UNDEFINED_MATERIAL,
        contact: Aspect = UNDEFINED_CONTACT_TYPE,
        callback: (FullContactScan) -> Boolean) = fullContactCallbacks.add(CollisionCallback(material, contact, callback))

    fun withTerrainContactConstraint(gapSouth: Int = 5, configure: (ContactConstraint.() -> Unit)): ComponentKey {
        this.gapSouth = gapSouth
        val result = ContactConstraint.build(configure)
        terrainContactConstraintIndex = result.instanceIndex
        initTerrainContact()
        return result
    }

    fun withTerrainContactConstraint(gapSouth: Int = 5, constraintName: String): ComponentKey {
        this.gapSouth = gapSouth
        val result = ContactConstraint[constraintName]
        terrainContactConstraintIndex = result.index
        initTerrainContact()
        return ContactConstraint.getKey(result.index)
    }

    override fun resolve(entity: Entity, contact: EContact, contactScan: ContactScans) {
        val terrainContact = contactScan.getFullScan(terrainContactConstraintIndex)!!
        val movement = entity[EMovement]
        val prefGround = movement.onGround
        movement.onGround = false

        if (terrainContact.hasAnyContact())
            resolveTerrainContact(terrainContact, entity, movement, prefGround)

        if (fullContactConstraintIndex >= 0) {
            val fullContact = contactScan.getFullScan(fullContactConstraintIndex)!!

            // process callbacks first if available
            // stop processing on first callback returns true
            if (!fullContactCallbacks.isEmpty)
                fullContactCallbacks.forEach {
                    if (fullContact.hasMaterialContact(it.material) && it.callback(fullContact))
                        return
                    if (fullContact.hasContactOfType(it.contact) && it.callback(fullContact))
                        return
                }
        }
    }

    private fun resolveTerrainContact(contacts: FullContactScan, entity: Entity, movement: EMovement, prefGround: Boolean) {

        val transform = entity[ETransform]
        takeFullLedgeScans(contacts)
        resolveVertically(contacts, entity, transform, movement)
        resolveHorizontally(contacts, entity, transform, movement)

        if (!prefGround && movement.onGround)
            touchGroundCallback(entity.index)
        if (prefGround && !movement.onGround)
            looseGroundContactCallback(entity.index)
    }

    private fun resolveVertically(contacts: FullContactScan, entity: Entity, transform: ETransform, movement: EMovement) {
        var refresh = false
        var setOnGround = false
        val onSlope = contactSensorB1.cardinality != 0 &&
                contactSensorB3.cardinality != 0 &&
                contactSensorB1.cardinality != contactSensorB3.cardinality &&
                contactSensorGround.cardinality < contactSensorGround.width

        //println("onSlope $onSlope")

        if (onSlope && movement.velocity.v1 >= ZERO_FLOAT) {
            //println("adjust slope ${contactSensorB2.cardinality}")
            if (contactSensorB1.cardinality > contactSensorB3.cardinality) {
                //println("slope south-east")
                transform.move(dy = -(contactSensorB1.cardinality - gapSouth))
                transform.position.y = ceil(transform.position.y)
                movement.velocity.v1 = ZERO_FLOAT
                refresh = true
                setOnGround = true
                onSlopeCallback(entity.index, contactSensorB1.cardinality - contactSensorB3.cardinality, contacts)
            } else {
                //println("slope south-west")
                transform.move(dy = -(contactSensorB3.cardinality - gapSouth))
                transform.position.y = ceil(transform.position.y)
                movement.velocity.v1 = ZERO_FLOAT
                refresh = true
                setOnGround = true
                onSlopeCallback(entity.index, contactSensorB1.cardinality - contactSensorB3.cardinality, contacts)
            }
        } else if (bmax > gapSouth && movement.velocity.v1 >= ZERO_FLOAT) {
            //println("adjust ground: ${bmax - gapSouth} : ${movement.velocity.v1 }")
            transform.move(dy = -(bmax - gapSouth))
            transform.position.y = ceil(transform.position.y)
            movement.velocity.v1 = ZERO_FLOAT
            refresh = true
            setOnGround = true
        }

        if (tmax > 0) {
            //println("adjust top: $tmax")
            transform.move(dy = tmax)
            transform.position.y = floor(transform.position.y)
            if (movement.velocity.v1 < ZERO_FLOAT)
                movement.velocity.v1 = ZERO_FLOAT
            refresh = true
        }

        if (refresh) {
            updateContacts(entity)
            takeFullLedgeScans(contacts)
        }

        //println("contactSensorGround.cardinality ${contactSensorGround.cardinality}")

        movement.onGround =
            setOnGround || contactSensorGround.cardinality > 0
        if (movement.onGround)
            transform.position.y = ceil(transform.position.y)

        //println("onGround ${movement.onGround}")
    }

    private fun resolveHorizontally(contacts: FullContactScan, entity: Entity, transform: ETransform, movement: EMovement) {
        var refresh = false

        if (lmax > 0) {
            //println("adjust left: $lmax")
            transform.move(dx = lmax)
            transform.position.x = floor(transform.position.x)
            movement.velocity.v0 = ZERO_FLOAT
            refresh = true
        }

        if (rmax > 0) {
            //println("adjust right: $rmax")
            transform.move(dx = -rmax)
            transform.position.x = ceil(transform.position.x)
            movement.velocity.v0 = ZERO_FLOAT
            refresh = true
        }

        if (refresh) {
            updateContacts(entity)
            takeFullLedgeScans(contacts)
        }
    }

    private fun takeFullLedgeScans(contacts: FullContactScan) {
        contactSensorT1.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorT2.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorT3.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorB1.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorB2.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorB3.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorL1.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorL2.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorL3.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorR1.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorR2.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorR3.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)
        contactSensorGround.clearMask().or(contacts.contactMask, contacts.contactMask.x, contacts.contactMask.y)


        tmax = max(contactSensorT1.cardinality, max(contactSensorT2.cardinality, contactSensorT3.cardinality))
        bmax = max(contactSensorB1.cardinality, max(contactSensorB2.cardinality, contactSensorB3.cardinality))
        lmax = max(contactSensorL1.cardinality, max(contactSensorL2.cardinality, contactSensorL3.cardinality))
        rmax = max(contactSensorR1.cardinality, max(contactSensorR2.cardinality, contactSensorR3.cardinality))
    }

    private fun initTerrainContact() {
        val constraint = ContactConstraint[terrainContactConstraintIndex]
        x2 = constraint.bounds.width / 2
        x3 = constraint.bounds.width - 3
        y2 = (constraint.bounds.height - gapSouth) / 2
        y3 = constraint.bounds.height - gapSouth - 3


        contactSensorT1.reset(x1, 0, 1, scanLength)
        contactSensorT2.reset(x2, 0, 1, scanLength)
        contactSensorT3.reset(x3, 0, 1, scanLength)

        contactSensorB1.reset(x1, constraint.bounds.height - bScanLength, 1, bScanLength)
        contactSensorB2.reset(x2, constraint.bounds.height - bScanLength, 1, bScanLength)
        contactSensorB3.reset(x3, constraint.bounds.height - bScanLength, 1, bScanLength)

        contactSensorL1.reset(0, y1, scanLength, 1)
        contactSensorL2.reset(0, y2, scanLength, 1)
        contactSensorL3.reset(0, y3, scanLength, 1)

        contactSensorR1.reset(constraint.bounds.width - scanLength, y1, scanLength, 1)
        contactSensorR2.reset(constraint.bounds.width - scanLength, y2, scanLength, 1)
        contactSensorR3.reset(constraint.bounds.width - scanLength, y3, scanLength, 1)

        contactSensorGround.reset(groundContactOffset, constraint.bounds.height - gapSouth, constraint.bounds.width - 2 * groundContactOffset, 1)
    }

    companion object :  ComponentSubTypeSystem<CollisionResolver, PlatformerCollisionResolver>(
        CollisionResolver,
        "PlatformerCollisionResolver") {

        override fun create() = PlatformerCollisionResolver()
        val VOID_ON_SLOPE_CALLBACK: (Int, Int, FullContactScan) -> Unit = { _,_,_ -> }
    }

    data class CollisionCallback(
        @JvmField val material: Aspect = UNDEFINED_MATERIAL,
        @JvmField val contact: Aspect = UNDEFINED_CONTACT_TYPE,
        @JvmField val callback: (FullContactScan) -> Boolean
    )
}