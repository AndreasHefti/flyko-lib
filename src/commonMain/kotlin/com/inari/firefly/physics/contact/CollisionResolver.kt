package com.inari.firefly.physics.contact

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGrid
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.MovementControl
import com.inari.util.collection.BitSet
import com.inari.util.event.Event
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector3i
import kotlin.math.ceil
import kotlin.math.floor

abstract class CollisionResolver protected constructor(): Component(CollisionResolver) {

    abstract fun resolve(entity: Entity, contact: EContact, contactScan: ContactScans)

    companion object : ComponentSystem<CollisionResolver>("ContactResolver") {
        override fun allocateArray(size: Int): Array<CollisionResolver?> = arrayOfNulls(size)
        override fun create(): CollisionResolver =
            throw UnsupportedOperationException("ContactResolver is abstract use a concrete implementation instead")

        // Contains all entity ids that has contact scans defined and are active
        // They are all processed during one contact scan cycle. A contact scan is triggered by a move event
        private val entitiesWithScan = BitSet()
        private val entityListener: ComponentEventListener = { index, type ->
            if (entityMatch(index)) {
                when(type) {
                    ComponentEventType.ACTIVATED -> entitiesWithScan[index] = true
                    ComponentEventType.DEACTIVATED -> entitiesWithScan[index] = false
                    else -> {}
                }
            }
        }

        private fun entityMatch(index: Int) : Boolean {
            val entity = Entity[index]
            return (EContact in entity.aspects &&
                ETransform in entity.aspects &&
                ETile !in entity.aspects &&
                    entity[EContact].contactScans.hasAnyScan)
        }

        init {
            MovementControl // load movement first to ensure Contact MapUpdate first
            Engine.registerListener(UPDATE_EVENT_TYPE, this::update)
        }

        private fun update() {
            var i = entitiesWithScan.nextSetBit(0)
            while (i >= 0) {
                val entity = Entity[i]
                i = entitiesWithScan.nextSetBit(i + 1)
                val contacts = entity[EContact]
                if (!contacts.contactScans.hasAnyScan)
                    continue

                scanContacts(entity, contacts)

                if (contacts.collisionResolverRef.exists) {
                    CollisionResolver[contacts.collisionResolverRef.targetKey]
                        .resolve(entity, contacts, contacts.contactScans)
                    ContactMap.update(entity)
                }

                if (contacts.notifyContacts && contacts.contactScans.hasAnyContact()) {
                    contactEvent.entityId = entity.index
                    Engine.notify(contactEvent)
                }
            }
        }

        private fun scanContacts(entity: Entity, contactsComp: EContact) {
            var i = 0
            while (i < contactsComp.contactScans.scans.capacity) {
                val c = contactsComp.contactScans.scans[i++] ?: continue
                updateContacts(entity, c)
            }
        }

        fun updateContacts(entityIndex: Int) = updateContacts(Entity[entityIndex])
        fun updateContacts(entity: Entity) {
            val contacts = entity[EContact]
            if (!contacts.contactScans.hasAnyScan)
                return

            scanContacts(entity, contacts)
        }

        private val originWorldBounds = SystemContactBounds(circle = Vector3i())
        private val otherWorldBounds = SystemContactBounds(circle = Vector3i())
        private fun updateContacts(entity: Entity, contactScan: ContactScan) {
            val constraint = contactScan.constraint
            val transform = entity[ETransform]
            val movement = entity[EMovement]

            var layerRef = constraint.layerIndex
            if (layerRef < 0)
                layerRef = transform.layerIndex

            contactScan.clear()

            // apply bounds of the contact shape within world coordinate system
            val position = getWorldPos(entity, transform)
            originWorldBounds.bounds(
                (if (movement.velocity.v0 > 0) ceil(position.x.toDouble()).toInt() else floor(position.x.toDouble()).toInt()) + constraint.bounds.x,
                (if (movement.velocity.v1 > 0) ceil(position.y.toDouble()).toInt() else floor(position.y.toDouble()).toInt()) + constraint.bounds.y,
            )
            if (constraint.isCircle) {
                originWorldBounds.circle!!.x = originWorldBounds.bounds.x
                originWorldBounds.circle.y = originWorldBounds.bounds.y
                originWorldBounds.circle.radius = constraint.bounds.radius
                originWorldBounds.bounds.x = originWorldBounds.bounds.x - constraint.bounds.radius
                originWorldBounds.bounds.y = originWorldBounds.bounds.y - constraint.bounds.radius
            } else {
                originWorldBounds.bounds.width = constraint.bounds.width
                originWorldBounds.bounds.height = constraint.bounds.height
            }

            scanTileContacts(entity, transform.viewIndex, layerRef, contactScan)
            scanSpriteContacts(entity, transform.viewIndex, layerRef, contactScan)
        }

        private val tempPos = Vector2f()
        private fun scanTileContacts(entity: Entity, viewRef: Int, layerRef: Int, contactScan: ContactScan) {
            val tileGrids = TileGrid[viewRef, layerRef]
            var gridIndex = tileGrids.nextSetBit(0)
            while (gridIndex >= 0) {
                val tileGrid = TileGrid[gridIndex]
                val iterator = tileGrid.tileGridIterator(originWorldBounds.bounds)
                while (iterator.hasNext()) {
                    val otherEntityRef = iterator.next()
                    if (entity.index == otherEntityRef)
                        continue

                    val otherEntity = Entity[otherEntityRef]
                    if (EContact !in otherEntity.aspects)
                        continue

                    val otherTransform = otherEntity[ETransform]
                    val otherContact = otherEntity[EContact]

                    //apply bounds of the other contact shape within world coordinate system
                    tempPos(
                        iterator.worldPosition.x + otherTransform.position.x,
                        iterator.worldPosition.y + otherTransform.position.y
                    )
                    applyContactBounds(otherWorldBounds, otherContact, tempPos.x, tempPos.y)
                    contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, otherEntity.index)
                }

                gridIndex = tileGrids.nextSetBit(gridIndex + 1)
            }
        }

        private fun scanSpriteContacts(entity: Entity, viewRef: Int, layerRef: Int, contactScan: ContactScan) {
            val contactMaps = ContactMap.VIEW_LAYER_MAPPING[viewRef, layerRef]
            var mapIndex = contactMaps.nextSetBit(0)
            while (mapIndex >= 0) {
                val contactMap = ContactMap[mapIndex]
                val iterator = contactMap[originWorldBounds.bounds, entity]
                mapIndex = contactMaps.nextSetBit(mapIndex + 1)
                while (iterator.hasNext()) {
                    val otherEntity = Entity[iterator.next()]
                    val otherContact = otherEntity[EContact]
                    val otherWorldPos = getWorldPos(otherEntity, otherEntity[ETransform])

                    if (EMultiplier in otherEntity.aspects) {
                        val multiplier = otherEntity[EMultiplier]
                        val iterator2 = multiplier.positions.iterator()
                        while (iterator2.hasNext()) {
                            applyContactBounds(
                                otherWorldBounds,
                                otherContact,
                                otherWorldPos.x + iterator2.next(),
                                otherWorldPos.y + iterator2.next())
                            contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, otherEntity.index)
                        }
                    } else {
                        applyContactBounds(otherWorldBounds, otherContact, otherWorldPos.x, otherWorldPos.y)
                        contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, otherEntity.index)
                    }
                }
            }
        }

        private fun applyContactBounds(
            contactBounds: SystemContactBounds,
            contactDef: EContact,
            worldPosX: Float,
            worldPosY: Float
        ) {
            if (contactDef.isCircle)
                contactBounds.applyCircle(
                    floor(worldPosX).toInt() + contactDef.contactBounds.bounds.x,
                    floor(worldPosY).toInt() + contactDef.contactBounds.bounds.y,
                    contactDef.contactBounds.bounds.radius
                )
            else
                contactBounds.applyRectangle(
                    floor(worldPosX).toInt() + contactDef.contactBounds.bounds.x,
                    floor(worldPosY).toInt() + contactDef.contactBounds.bounds.y,
                    contactDef.contactBounds.bounds.width,
                    contactDef.contactBounds.bounds.height
                )
            if (contactDef.hasContactMask)
                contactBounds.applyBitMask(contactDef.contactBounds.bitmask!!)
            else
                contactBounds.resetBitmask()
        }

        private fun getWorldPos(entity: Entity, transform: ETransform): Vector2f {
            return if (EChild in entity.aspects) {
                addTransformPos(entity[EChild].parentIndex)
                tempPos
            } else
                transform.position
        }

        private fun addTransformPos(parent: Int) {
            val parentEntity = Entity[parent]
            tempPos + parentEntity[ETransform].position
            if (EChild in parentEntity.aspects)
                addTransformPos(parentEntity[EChild].parentIndex)
        }

        private val contactEventType = Event.EventType("ContactEvent")
        private val contactEvent = ContactEvent(contactEventType)
    }

    class ContactEvent(override val eventType: EventType) : Event<(Int) -> Unit>() {
        var entityId: Int = -1
            internal set
        override fun notify(listener: (Int) -> Unit) { listener(entityId) }
    }
}