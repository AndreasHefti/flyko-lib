package com.inari.firefly.game.collision

import com.inari.firefly.*
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.physics.contact.*
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.IntConsumer
import com.inari.util.Predicate
import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField
import kotlin.math.*


class PlatformerCollisionResolver : CollisionResolver() {

    private var fullContactConstraintRef = -1
    private val fullContactCallbacks = DynArray.of<CollisionCallback>()
    private var solidContactAreaRef = -1
    private  lateinit var sensorMatrix: CollisionSensorMatrix

    @JvmField var slopeAdaptionThreshold = 15
    @JvmField var horizontalContactSensorThreshold = 10
    @JvmField var verticalContactSensorThreshold = 10
    @JvmField var touchGroundCallback: IntConsumer = EMPTY_INT_CONSUMER
    @JvmField var looseGroundContactCallback: IntConsumer = EMPTY_INT_CONSUMER
    @JvmField var onSlopeCallback: (Int, Int, Contacts) -> Unit = { _, _, _ -> }

    val withFullContactConstraint = ComponentRefResolver(ContactConstraint) {
        fullContactConstraintRef = it
    }

    fun <A : ContactConstraint> withSolidContactConstraint(
        gapNorth: Int = 1,
        gapEast: Int = 1,
        gapSouth: Int = 5,
        gapWest: Int = 1,
        builder: SystemComponentSingleType<A>, configure: (A.() -> Unit)): CompId {

        val result = builder.buildAndGet(configure)
        sensorMatrix = CollisionSensorMatrix(
            result.width - gapWest - gapEast,
            result.height - gapNorth - gapSouth,
            gapNorth,
            gapEast,
            gapSouth,
            gapWest)
        solidContactAreaRef = result.index
        return result.componentId
    }

    fun withSolidContactConstraint(
        constraintName: String,
        gapNorth: Int = 1,
        gapEast: Int = 1,
        gapSouth: Int = 5,
        gapWest: Int = 1): CompId {

        val result = ContactSystem.constraints[constraintName]
        sensorMatrix = CollisionSensorMatrix(
            result.width - gapWest - gapEast,
            result.height - gapNorth - gapSouth,
            gapNorth,
            gapEast,
            gapSouth,
            gapWest)
        solidContactAreaRef = result.index
        return result.componentId
    }

    fun withFullContactCallback(
        material: Aspect = UNDEFINED_MATERIAL,
        contact: Aspect = UNDEFINED_CONTACT_TYPE,
        callback: Predicate<Contacts>) {
        fullContactCallbacks.add(CollisionCallback(material, contact, callback))
    }

    override fun resolve(entity: Entity, contact: EContact, contactScan: ContactScan) {
        val terrainContact = contactScan[solidContactAreaRef]
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

    private fun minDist(d1: Int, d2: Int, d3: Int) : Int {
        var result = if (d1 != sensorMatrix.noDistanceValue) d1 else 0
        result = if (d2 != sensorMatrix.noDistanceValue) min(result, d2) else result
        return if (d3 != sensorMatrix.noDistanceValue) min(result, d3) else result
    }
    private fun minDist(d1: Int, d2: Int) : Int {
        val result = if (d1 != sensorMatrix.noDistanceValue) d1 else 0
        return if (d2 != sensorMatrix.noDistanceValue) min(result, d2) else result
    }

    private fun resolveTerrainContact(contacts: Contacts, entity: Entity, transform: ETransform, movement: EMovement) {
        //println("movement: ${movement.velocity}")
        //println("$sensorMatrix")

        val prefGround = movement.onGround
        sensorMatrix.calc(contacts.contactMask)

        resolveHorizontally(entity, contacts, movement, transform)
        resolveVertically(contacts, movement, transform, entity)

        if (movement.onGround)
            transform.position.y = ceil(transform.position.y)
        if (!prefGround && movement.onGround)
            touchGroundCallback(entity.index)
        if (prefGround && !movement.onGround)
            looseGroundContactCallback(entity.index)
    }

    private fun resolveVertically(
        contacts: Contacts,
        movement: EMovement,
        transform: ETransform,
        entity: Entity
    ) {
        var refresh = false
        var setOnGround = false
        if (contacts.hasAnyContact()) {
            if (movement.velocity.dy < 0.0f) {

                val d1 = getAdjustDistance(sensorMatrix.distances[0].dy, verticalContactSensorThreshold)
                val d2 = getAdjustDistance(sensorMatrix.distances[1].dy, verticalContactSensorThreshold)
                val d3 = getAdjustDistance(sensorMatrix.distances[2].dy, verticalContactSensorThreshold)
                val minDistTop = minDist(d1, d2, d3)

                // special case jump under slope
                if ((sensorMatrix.distances[0].dy != sensorMatrix.noDistanceValue &&
                    sensorMatrix.contactSensorLineLeft.cardinality < verticalContactSensorThreshold &&
                    sensorMatrix.contactSensorLineLeft.getBit(0, sensorMatrix.gapNorth)) ||
                    (sensorMatrix.distances[2].dy != sensorMatrix.noDistanceValue &&
                    sensorMatrix.contactSensorLineRight.cardinality < verticalContactSensorThreshold &&
                    sensorMatrix.contactSensorLineRight.getBit(0, sensorMatrix.gapNorth))) {

                    movement.velocity.dy = 0.0f
                    refresh = true
                } else if (minDistTop != sensorMatrix.noDistanceValue && minDistTop < 0) {
                    movement.velocity.dy = 0.0f
                    refresh = true
                }
            } else {

                val d1 = getAdjustDistance(sensorMatrix.distances[5].dy, verticalContactSensorThreshold)
                val d2 = getAdjustDistance(sensorMatrix.distances[6].dy, verticalContactSensorThreshold)
                val d3 = getAdjustDistance(sensorMatrix.distances[7].dy, verticalContactSensorThreshold)
                //println("slope d1 $d1 d2 $d2 d3 $d3")
                if (movement.onGround) {
                    // adjust slope
                    if ((d1 < 0 && d3 >= 0) || (d2 > 0 && d1 == 0)) {
                        // slope south-west
                        val d4 = abs(d3 - d1)
                        if (d4 < slopeAdaptionThreshold) {
                            //println("adjust slope south-west d4 $d4 d2 $d2")
                            transform.move(dy = d2)
                            onSlopeCallback(entity.index, d2, contacts)
                            refresh = true
                            setOnGround = true
                        }
                    } else if ((d1 >= 0 && d3 < 0) || (d2 > 0 && d3 == 0)) {
                        // slope south-east
                        val d4 = abs(d1 - d3)
                        if (d4 < slopeAdaptionThreshold) {
                            //println("adjust slope south-east d4 $d4 d2 $d2")
                            transform.move(dy = d2)
                            onSlopeCallback(entity.index, d2, contacts)
                            refresh = true
                            setOnGround = true

                        }
                    }
                } else {
                    val minDistBottom = if (d1 != 0 && d2 != 0) min(d1, d2) else if (d2 != 0 && d3 != 0) min(d2, d3) else d2
                    if (minDistBottom != sensorMatrix.noDistanceValue && minDistBottom < 0) {
                        //println("adjust ground: $minDistBottom")
                        transform.move(dy = minDistBottom)
                        refresh = true
                    }
                }
            }
        }

        if (refresh) {
            ContactSystem.updateContacts(entity.componentId)
            sensorMatrix.calc(contacts.contactMask)
        }

        // set on ground
        movement.onGround =
            setOnGround ||
            (movement.velocity.dy >= 0.0f &&
            (sensorMatrix.distances[5].dy == 0 ||
            sensorMatrix.distances[6].dy == 0 ||
            sensorMatrix.distances[7].dy == 0))

    }

    private fun getAdjustDistance(d: Int, max: Int): Int =
        if (d == sensorMatrix.noDistanceValue || abs(d) > max) 0 else d

    private fun resolveHorizontally(
        entity: Entity,
        contacts: Contacts,
        movement: EMovement,
        transform: ETransform,
    ) {
        var refresh = false
        if (movement.velocity.dx > 0.0f) {
            if (sensorMatrix.contactSensorLineRight.cardinality > horizontalContactSensorThreshold || sensorMatrix.distances[2].dx != 0) {
                val minDistRight = minDist(sensorMatrix.distances[2].dx, sensorMatrix.distances[4].dx)
                //println("minDistRight $minDistRight")
                if (minDistRight != sensorMatrix.noDistanceValue && minDistRight < 0) {
                    //println("adjust right $minDistRight")
                    transform.move(dx = minDistRight)
                    transform.position.x = ceil(transform.position.x)
                    refresh = true
                }
            }
        } else if (movement.velocity.dx < 0.0f) {
            if (sensorMatrix.contactSensorLineLeft.cardinality > horizontalContactSensorThreshold || sensorMatrix.distances[0].dx != 0) {
                val minDistLeft = minDist(sensorMatrix.distances[0].dx, sensorMatrix.distances[3].dx)
                if (minDistLeft != sensorMatrix.noDistanceValue && minDistLeft < 0) {
                    //println("adjust left $minDistLeft")
                    transform.move(dx = -minDistLeft)
                    transform.position.x = floor(transform.position.x)
                    refresh = true
                }
            }
        }

        if (refresh) {
            ContactSystem.updateContacts(entity.componentId)
            sensorMatrix.calc(contacts.contactMask)
        }
    }

    companion object : SystemComponentSubType<CollisionResolver, PlatformerCollisionResolver>(CollisionResolver, PlatformerCollisionResolver::class) {
        override fun createEmpty() = PlatformerCollisionResolver()
    }

    private data class CollisionCallback(
        @JvmField val material: Aspect = UNDEFINED_MATERIAL,
        @JvmField val contact: Aspect = UNDEFINED_CONTACT_TYPE,
        @JvmField val callback: Predicate<Contacts>
    )

}