package com.inari.firefly.game.actor

import com.inari.firefly.core.*

import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.contact.*
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.Movement.BasicMovementAspect.*
import com.inari.util.ZERO_FLOAT
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

    @JvmField var groundContactOffset = 2
    @JvmField val terrainContactConstraintRef = CReference(ContactConstraint) { initTerrainContact() }


    fun withTerrainContactConstraint(gapSouth: Int = 5, configure: (ContactConstraint.() -> Unit)): ComponentKey {
        this.gapSouth = gapSouth
        val result = ContactConstraint.build(configure)
        terrainContactConstraintRef(result)
        return result
    }

    fun withTerrainContactConstraint(gapSouth: Int = 5, constraintName: String): ComponentKey {
        this.gapSouth = gapSouth
        val result = ContactConstraint[constraintName]
        terrainContactConstraintRef(result)
        return ContactConstraint.getKey(result.index)
    }

    override fun resolve(entity: Entity, contact: EContact, contactScan: ContactScans) {
        if (terrainContactConstraintRef.exists) {
            val terrainContact = contactScan.getFullScan(terrainContactConstraintRef.targetKey.componentIndex)!!
            if (terrainContact.hasAnyContact()) {
                val movement = entity[EMovement]
                val prefGround = movement.onGround
                movement.onGround = false
                resolveTerrainContact(terrainContact, entity, movement, prefGround)
            }
        }

    }

    private fun resolveTerrainContact(contacts: FullContactScan, entity: Entity, movement: EMovement, prefGround: Boolean) {
        val transform = entity[ETransform]
        takeFullLedgeScans(contacts)
        resolveVertically(contacts, entity, transform, movement)
        resolveHorizontally(contacts, entity, transform, movement)

        movement.aspects[GROUND_TOUCHED] = (!prefGround && movement.onGround)
        movement.aspects[GROUND_LOOSE] = (prefGround && !movement.onGround)
    }

    private fun resolveVertically(contacts: FullContactScan, entity: Entity, transform: ETransform, movement: EMovement) {

        var refresh = false
        var setOnGround = false
        movement.aspects[ON_SLOPE_DOWN] = false
        movement.aspects[ON_SLOPE_UP] = false
        movement.aspects[BLOCK_NORTH] = false

        val onSlope = contactSensorB1.cardinality != 0 &&
                contactSensorB3.cardinality != 0 &&
                contactSensorB1.cardinality != contactSensorB3.cardinality &&
                contactSensorGround.cardinality < contactSensorGround.width

        //println("onSlope $onSlope")

        if (onSlope && movement.velocity.v1 >= ZERO_FLOAT) {
            //println("adjust slope")
            if (contactSensorB1.cardinality > contactSensorB3.cardinality) {
                val gap = contactSensorB1.cardinality - gapSouth
                if (gap >= -1) {
                    transform.move(dy = -gap)
                    transform.position.y = ceil(transform.position.y)
                    movement.velocity.v1 = ZERO_FLOAT
                    refresh = true
                    setOnGround = true
                    val slopeO = (contactSensorB1.cardinality - contactSensorB3.cardinality > 0)
                    movement.aspects[ON_SLOPE_DOWN] = slopeO
                    movement.aspects[ON_SLOPE_UP] = !slopeO
                }
            } else {
                //println("slope south-west")
                val gap = contactSensorB3.cardinality - gapSouth
                if (gap >= -1) {
                    transform.move(dy = -gap)
                    transform.position.y = ceil(transform.position.y)
                    movement.velocity.v1 = ZERO_FLOAT
                    refresh = true
                    setOnGround = true
                    val slopeO = (contactSensorB1.cardinality - contactSensorB3.cardinality > 0)
                    movement.aspects[ON_SLOPE_DOWN] = slopeO
                    movement.aspects[ON_SLOPE_UP] = !slopeO
                }
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
            movement.aspects[BLOCK_NORTH] = true
        }

        if (refresh) {
            updateContacts(entity)
            takeFullLedgeScans(contacts)
        }

        //println("contactSensorGround.cardinality ${contactSensorGround.cardinality}")
        movement.onGround =
            setOnGround || (movement.velocity.y >= 0f && contactSensorGround.cardinality > 0)
        if (movement.onGround)
            transform.position.y = ceil(transform.position.y)

        //println("onGround ${movement.onGround}")
    }

    private fun resolveHorizontally(contacts: FullContactScan, entity: Entity, transform: ETransform, movement: EMovement) {
        var refresh = false

        movement.aspects[SLIP_LEFT] = false
        movement.aspects[SLIP_RIGHT] = false
        movement.aspects[BLOCK_EAST] = false
        movement.aspects[BLOCK_WEST] = false

        if (lmax > 0) {
            //println("adjust left: $lmax ${movement.velocity.v0 }")
            transform.move(dx = lmax)
            transform.position.x = floor(transform.position.x)
            movement.aspects[SLIP_RIGHT] = movement.velocity.v0 > -1f
            movement.aspects[BLOCK_WEST] = movement.velocity.v0 <= 0f
            movement.velocity.v0 = ZERO_FLOAT
            refresh = true

        }

        if (rmax > 0) {
            //println("adjust right: $rmax ${movement.velocity.v0 }")
            transform.move(dx = -rmax)
            transform.position.x = ceil(transform.position.x)
            movement.aspects[SLIP_LEFT] = movement.velocity.v0 < 1f
            movement.aspects[BLOCK_EAST] = movement.velocity.v0 >= 0f
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
        val constraint = ContactConstraint[terrainContactConstraintRef.targetKey.componentIndex]
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

    companion object : SubComponentBuilder<CollisionResolver, PlatformerCollisionResolver>(CollisionResolver) {
        override fun create() = PlatformerCollisionResolver()
        val VOID_ON_SLOPE_CALLBACK: (Int, Int, FullContactScan) -> Unit = { _,_,_ -> }
    }
}