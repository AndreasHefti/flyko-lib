package com.inari.firefly.game.collision

import com.inari.firefly.EMPTY_INT_CONSUMER
import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.ZERO_FLOAT
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.game.VOID_LOOS_GROUND_CALLBACK
import com.inari.firefly.game.VOID_ON_SLOPE_CALLBACK
import com.inari.firefly.game.VOID_TOUCH_GROUND_CALLBACK
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.physics.contact.*
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.IntConsumer
import com.inari.util.Predicate
import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynArray
import com.inari.util.geom.BitMask
import kotlin.jvm.JvmField
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class PlatformerCollisionResolver : CollisionResolver()  {

    private var fullContactConstraintRef = -1
    private val fullContactCallbacks = DynArray.of<CollisionCallback>()
    private var terrainContactConstraintRef = -1

    @JvmField var touchGroundCallback = VOID_TOUCH_GROUND_CALLBACK
    @JvmField var looseGroundContactCallback = VOID_LOOS_GROUND_CALLBACK
    @JvmField var onSlopeCallback = VOID_ON_SLOPE_CALLBACK

    val withFullContactConstraint = ComponentRefResolver(ContactConstraint) {
        fullContactConstraintRef = it
    }

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

    fun withFullContactCallback(
        material: Aspect = UNDEFINED_MATERIAL,
        contact: Aspect = UNDEFINED_CONTACT_TYPE,
        callback: Predicate<Contacts>) {
        fullContactCallbacks.add(CollisionCallback(material, contact, callback))
    }

    fun <A : ContactConstraint> withTerrainContactConstraint(gapSouth: Int = 5, builder: SystemComponentSingleType<A>, configure: (A.() -> Unit)): CompId {
        this.gapSouth = gapSouth
        val result = builder.build(configure)
        terrainContactConstraintRef = result.index
        initTerrainContact()
        return result
    }

    fun withTerrainContactConstraint(constraintName: String, gapSouth: Int = 5): CompId {
        this.gapSouth = gapSouth
        val result = ContactSystem.constraints[constraintName]
        terrainContactConstraintRef = result.componentId.index
        initTerrainContact()
        return result.componentId
    }

    fun withTerrainContactConstraint(constraintRefId: CompId, gapSouth: Int = 5): CompId {
        this.gapSouth = gapSouth
        val result = ContactSystem.constraints[constraintRefId]
        terrainContactConstraintRef = result.componentId.index
        initTerrainContact()
        return result.componentId
    }

    override fun resolve(entity: Entity, contact: EContact, contactScan: ContactScan) {

        val terrainContact = contactScan[terrainContactConstraintRef]
        if (terrainContact.hasAnyContact()) {
            val transform = entity[ETransform]
            val movement = entity[EMovement]
            resolveTerrainContact(terrainContact, entity, transform, movement)
        }

        if (fullContactConstraintRef >= 0) {
            val fullContact = contactScan[fullContactConstraintRef]

            // process callbacks first if available
            // stop processing on first callback returns true
            if (!fullContactCallbacks.isEmpty)
                fullContactCallbacks.forEach {
                    if (fullContact.hasMaterialContact(it.material) && it.callback(fullContact))
                        return
                    if (fullContact.hasContact(it.contact) && it.callback(fullContact))
                        return
                }
        }
    }

    private fun resolveTerrainContact(contacts: Contacts, entity: Entity, transform: ETransform, movement: EMovement) {

        val prefGround = movement.onGround

        takeFullLedgeScans(contacts)
        resolveVertically(contacts, entity, transform, movement)
        resolveHorizontally(contacts, entity, transform, movement)

        if (!prefGround && movement.onGround)
            touchGroundCallback(entity.index)
        if (prefGround && !movement.onGround)
            looseGroundContactCallback(entity.index)
    }

    private fun resolveVertically(contacts: Contacts, entity: Entity, transform: ETransform, movement: EMovement) {
        var refresh = false
        var setOnGround = false
        val onSlope = contactSensorB1.cardinality != 0 &&
                contactSensorB3.cardinality != 0 &&
                contactSensorB1.cardinality != contactSensorB3.cardinality &&
                contactSensorGround.cardinality < contactSensorGround.width

        //println("onSlope $onSlope")

        if (onSlope && movement.velocity.dy >= 0.0f) {
            //println("adjust slope ${contactSensorB2.cardinality}")
            if (contactSensorB1.cardinality > contactSensorB3.cardinality) {
                //println("slope south-east")
                transform.move(dy = -(contactSensorB1.cardinality - gapSouth))
                transform.position.y = ceil(transform.position.y)
                movement.velocity.dy = 0.0f
                refresh = true
                setOnGround = true
                onSlopeCallback(entity.index, contactSensorB1.cardinality - contactSensorB3.cardinality, contacts)
            } else {
                //println("slope south-west")
                transform.move(dy = -(contactSensorB3.cardinality - gapSouth))
                transform.position.y = ceil(transform.position.y)
                movement.velocity.dy = 0.0f
                refresh = true
                setOnGround = true
                onSlopeCallback(entity.index, contactSensorB1.cardinality - contactSensorB3.cardinality, contacts)
            }
        } else if (bmax > gapSouth && movement.velocity.dy >= 0.0f) {
            //println("adjust ground: ${bmax - gapSouth} : ${movement.velocity.dy}")
            transform.move(dy = -(bmax - gapSouth))
            transform.position.y = ceil(transform.position.y)
            movement.velocity.dy = 0.0f
            refresh = true
            setOnGround = true
        }

        if (tmax > 0) {
            //println("adjust top: $tmax")
            transform.move(dy = tmax)
            transform.position.y = floor(transform.position.y)
            if (movement.velocity.dy < 0.0f)
                movement.velocity.dy = 0.0f
            refresh = true
        }

        if (refresh) {
            ContactSystem.updateContacts(entity.componentId)
            takeFullLedgeScans(contacts)
        }

        movement.onGround =
            setOnGround || contactSensorGround.cardinality > 0
        if (movement.onGround)
            transform.position.y = ceil(transform.position.y)

        //println("onGround ${movement.onGround}")
    }

    private fun resolveHorizontally(contacts: Contacts, entity: Entity, transform: ETransform, movement: EMovement) {
        var refresh = false

        if (lmax > 0) {
            //println("adjust left: $lmax")
            transform.move(dx = lmax)
            transform.position.x = floor(transform.position.x)
            movement.velocity.dx = ZERO_FLOAT
            refresh = true
        }

        if (rmax > 0) {
            //println("adjust right: $rmax")
            transform.move(dx = -rmax)
            transform.position.x = ceil(transform.position.x)
            movement.velocity.dx = ZERO_FLOAT
            refresh = true
        }

        if (refresh) {
            ContactSystem.updateContacts(entity.componentId)
            takeFullLedgeScans(contacts)
        }
    }

    private fun takeFullLedgeScans(contacts: Contacts) {
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
        val constraint = ContactSystem.constraints[terrainContactConstraintRef]
        x2 = constraint.width / 2
        x3 = constraint.width - 3
        y2 = (constraint.height - gapSouth) / 2
        y3 = constraint.height - gapSouth - 3


        contactSensorT1.reset(x1, 0, 1, scanLength)
        contactSensorT2.reset(x2, 0, 1, scanLength)
        contactSensorT3.reset(x3, 0, 1, scanLength)

        contactSensorB1.reset(x1, constraint.height - bScanLength, 1, bScanLength)
        contactSensorB2.reset(x2, constraint.height - bScanLength, 1, bScanLength)
        contactSensorB3.reset(x3, constraint.height - bScanLength, 1, bScanLength)

        contactSensorL1.reset(0, y1, scanLength, 1)
        contactSensorL2.reset(0, y2, scanLength, 1)
        contactSensorL3.reset(0, y3, scanLength, 1)

        contactSensorR1.reset(constraint.width - scanLength, y1, scanLength, 1)
        contactSensorR2.reset(constraint.width - scanLength, y2, scanLength, 1)
        contactSensorR3.reset(constraint.width - scanLength, y3, scanLength, 1)

        contactSensorGround.reset(x1, constraint.height - gapSouth, constraint.width - 4, 1)
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<CollisionResolver, PlatformerCollisionResolver>(CollisionResolver, PlatformerCollisionResolver::class) {
        override fun createEmpty() = PlatformerCollisionResolver()
    }
}