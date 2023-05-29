package com.inari.firefly.physics.contact

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGrid
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.MovementSystem
import com.inari.util.collection.BitSet
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector3i
import kotlin.math.ceil
import kotlin.math.floor

abstract class CollisionResolver protected constructor(): Component(CollisionResolver) {

    abstract fun resolve(index: EntityIndex, contact: EContact, contactScan: ContactScans)

    companion object : AbstractComponentSystem<CollisionResolver>("ContactResolver") {
        override fun allocateArray(size: Int): Array<CollisionResolver?> = arrayOfNulls(size)

        // Contains all entity ids that has contact scans defined and are active
        // They are all processed during one contact scan cycle. A contact scan is triggered by a move event
        private val entitiesWithScan = BitSet()

        private fun entityListener(key: ComponentKey, type: ComponentEventType) {
            if (!entityMatch(key.componentIndex)) return
            if (type == ComponentEventType.ACTIVATED)
                entitiesWithScan[key.componentIndex] = true
            else if (type == ComponentEventType.DEACTIVATED)
                entitiesWithScan[key.componentIndex] = false
        }

        private fun entityMatch(index: EntityIndex): Boolean {
            val entity = Entity[index]
            return (EContact in entity.aspects &&
                    ETransform in entity.aspects &&
                    ETile !in entity.aspects &&
                    EContact[index].contactScans.hasAnyScan)
        }

        init {
            MovementSystem // load movement first to ensure Contact MapUpdate first
            Entity.registerComponentListener(::entityListener)
            Engine.registerListener(UPDATE_EVENT_TYPE, ::update)
        }

        fun updateContacts(entityIndex: EntityIndex) {
            val contacts = EContact[entityIndex]
            if (!contacts.contactScans.hasAnyScan)
                return

            scanContacts(entityIndex, contacts)
        }

        private fun update() {
            var i = entitiesWithScan.nextSetBit(0)
            while (i >= 0) {
                if (Pausing.isPaused(Entity[i].groups)) {
                    i = entitiesWithScan.nextSetBit(i + 1)
                    continue
                }

                val contacts = EContact[i]
                if (!contacts.contactScans.hasAnyScan) {
                    i = entitiesWithScan.nextSetBit(i + 1)
                    continue
                }

                scanContacts(i, contacts)

                if (contacts.collisionResolverRef.exists) {
                    CollisionResolver[contacts.collisionResolverRef.targetKey]
                        .resolve(i, contacts, contacts.contactScans)
                    ContactMap.update(i)
                }

                if (contacts.contactConstraintRef.exists)
                    processContactCallbacks(i, contacts)

                i = entitiesWithScan.nextSetBit(i + 1)
            }
        }

        private fun processContactCallbacks(index: EntityIndex, contacts: EContact) {
            val fullContact = contacts.contactScans.getFullScan(contacts.contactConstraintRef.targetKey.componentIndex)
            if (fullContact == null || !fullContact.hasAnyContact())
                return

            // process callbacks first if available
            // stop processing on first callback returns true
            var i = contacts.contactCallbacks.nextIndex(0)
            while (i >= 0) {
                val contactCallback = contacts.contactCallbacks[i]
                    ?: continue

                val callback =
                    ((contactCallback.materialType == EContact.UNDEFINED_MATERIAL || fullContact.hasMaterialContact(
                        contactCallback.materialType
                    )) ||
                            (contactCallback.contactType == EContact.UNDEFINED_CONTACT_TYPE || fullContact.hasContactOfType(
                                contactCallback.contactType
                            )))

                if (callback && contactCallback.callback(Entity.getKey(index), fullContact))
                    return

                i = contacts.contactCallbacks.nextIndex(i + 1)
            }
        }

        private fun scanContacts(index: EntityIndex, contactsComp: EContact) {
            var i = 0
            while (i < contactsComp.contactScans.scans.capacity) {
                val c = contactsComp.contactScans.scans[i++] ?: continue
                updateContacts(index, c)
            }
        }

        private val originWorldBounds = SystemContactBounds(circle = Vector3i())
        private val otherWorldBounds = SystemContactBounds(circle = Vector3i())
        private fun updateContacts(index: EntityIndex, contactScan: ContactScan) {
            val constraint = contactScan.constraint
            val transform = ETransform[index]
            val movement = EMovement[index]

            var layerRef = constraint.layerIndex
            if (layerRef < 0)
                layerRef = transform.layerIndex

            contactScan.clear()

            // apply bounds of the contact shape within world coordinate system
            val position = getWorldPos(index)
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

            scanTileContacts(index, transform.viewIndex, layerRef, contactScan)
            scanSpriteContacts(index, transform.viewIndex, layerRef, contactScan)
        }

        private val tempPos = Vector2f()
        private fun scanTileContacts(
            index: EntityIndex,
            viewRef: ComponentIndex,
            layerRef: ComponentIndex,
            contactScan: ContactScan
        ) {

            val tileGrids = TileGrid[viewRef, layerRef]
            var gridIndex = tileGrids.nextSetBit(0)
            while (gridIndex >= 0) {
                val tileGrid = TileGrid[gridIndex]
                val iterator = tileGrid.tileGridIterator(originWorldBounds.bounds)
                while (iterator.hasNext()) {
                    val otherEntityIndex = iterator.next()
                    if (index == otherEntityIndex)
                        continue

                    //val otherEntity = Entity[otherEntityRef]
                    if (otherEntityIndex !in EContact)
                        continue

                    val otherTransform = ETransform[otherEntityIndex]
                    val otherContact = EContact[otherEntityIndex]
                    //apply bounds of the other contact shape within world coordinate system
                    tempPos(
                        iterator.worldPosition.x + otherTransform.position.x,
                        iterator.worldPosition.y + otherTransform.position.y
                    )
                    applyContactBounds(otherWorldBounds, otherContact, tempPos.x, tempPos.y)
                    contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, index)
                }

                gridIndex = tileGrids.nextSetBit(gridIndex + 1)
            }
        }

        private fun scanSpriteContacts(
            index: EntityIndex,
            viewRef: ComponentIndex,
            layerRef: ComponentIndex,
            contactScan: ContactScan
        ) {

            val contactMaps = ContactMap.VIEW_LAYER_MAPPING[viewRef, layerRef]
            var mapIndex = contactMaps.nextSetBit(0)
            while (mapIndex >= 0) {
                val contactMap = ContactMap[mapIndex]
                val iterator = contactMap[originWorldBounds.bounds, index]
                mapIndex = contactMaps.nextSetBit(mapIndex + 1)

                while (iterator.hasNext()) {
                    //val otherEntity = Entity[iterator.nextInt()]
                    val otherEntityIndex = iterator.nextInt()
                    val otherContact = EContact[otherEntityIndex]
                    val otherWorldPos = getWorldPos(otherEntityIndex)

                    if (otherEntityIndex in EMultiplier) {
                        val multiplier = EMultiplier[otherEntityIndex]
                        val iterator2 = multiplier.positions.iterator()
                        while (iterator2.hasNext()) {
                            applyContactBounds(
                                otherWorldBounds,
                                otherContact,
                                otherWorldPos.x + iterator2.next(),
                                otherWorldPos.y + iterator2.next()
                            )
                            contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, index)
                        }
                    } else {
                        applyContactBounds(otherWorldBounds, otherContact, otherWorldPos.x, otherWorldPos.y)
                        contactScan.scanFullContact(originWorldBounds, otherWorldBounds, otherContact, otherEntityIndex)
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

        private fun getWorldPos(index: EntityIndex): Vector2f {
            return if (index in EChild) {
                addTransformPos(EChild[index].parentIndex)
                tempPos
            } else
                ETransform[index].position
        }

        private fun addTransformPos(parent: EntityIndex) {
            tempPos + ETransform[parent].position
            if (parent in EChild)
                addTransformPos(EChild[parent].parentIndex)
        }
    }
}