package com.inari.firefly.game.player

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.ZERO_FLOAT
import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector2i

class PlayerComposite private constructor(override var name: String = PLAYER_COMPOSITE_NAME) : GenericComposite() {

    var entityId = NO_COMP_ID
        internal set
    var playerPosition = Vector2f()
        internal set
    var playerPivot = Vector2i()
        internal set
    var playerVelocity = Vector2f()
        internal set

    override fun loadComposite() {
        super.loadComposite()

        // set quick references
        val entity = FFContext[Entity, PLAYER_ENTITY_NAME]
        entityId = entity.componentId
        playerPosition = entity[ETransform].position
        playerPivot(entity[ETransform].pivot)
        playerVelocity = if (EMovement in entity.aspects)
            FFContext[EMovement, entityId].velocity
        else
            Vector2f(ZERO_FLOAT, ZERO_FLOAT)
        // notify
        PlayerEvent.send(PlayerEventType.PLAYER_LOADED)
    }

    override fun activateComposite() {
        super.activateComposite()

        FFContext.activate(Entity, PLAYER_ENTITY_NAME)

        // notify
        PlayerEvent.send(PlayerEventType.PLAYER_ACTIVATED)
    }

    override fun deactivateComposite() {
        super.deactivateComposite()

        FFContext.deactivate(Entity, PLAYER_ENTITY_NAME)

        // notify
        PlayerEvent.send(PlayerEventType.PLAYER_DEACTIVATED)
    }

    override fun disposeComposite() {
        super.disposeComposite()

        // delete entity
        FFContext.delete(Entity, PLAYER_ENTITY_NAME)

        // clear data
        entityId = NO_COMP_ID
        playerPosition = Vector2f()
        playerPivot(0, 0)

        // notify
        PlayerEvent.send(PlayerEventType.PLAYER_DELETED)
    }

    //override fun componentType() = Builder
    companion object {
        const val PLAYER_COMPOSITE_NAME = "FF_PLAYER"
        const val PLAYER_ENTITY_NAME = "FF_PLAYER_ENTITY"
    }

    internal object Builder : SystemComponentSubType<Composite, PlayerComposite>(Composite, PlayerComposite::class) {
        override fun createEmpty() = PlayerComposite()
    }

}