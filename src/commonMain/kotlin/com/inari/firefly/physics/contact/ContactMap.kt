package com.inari.firefly.physics.contact

import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.util.collection.BitSet
import com.inari.util.geom.Rectangle
import kotlin.jvm.JvmField

abstract class ContactMap protected constructor() : SystemComponent(ContactMap::class.simpleName!!), ViewLayerAware {

    @JvmField internal var viewRef = -1
    @JvmField internal var layerRef = -1

    val view = ComponentRefResolver(View) { index->
        viewRef = setIfNotInitialized(index, "view")
    }
    val layer = ComponentRefResolver(Layer) { index->
        layerRef = setIfNotInitialized(index, "layer")
    }

    override val viewIndex: Int
        get() = viewRef
    override val layerIndex: Int
        get() = layerRef

    /** Use this to directly register an entity id for a specified ContactPool instance.
     * This first checks if the Entity is a valid entity by checking the availability of
     * an ETransform and an EContact component and the absence of an ETile component.
     *
     * (Tiles are not supported by ContactPools, they get checked against contacts within their TileGrid context)
     *
     * The equality of View and Layer id of the Entities Transform must match the View and Layer id of the ContactPool
     * to get the entityId registered in a ContactPool.
     * All this check are done by registering, and a User has not to concern about that.
     *
     * @param entity the Entity to add / register
     */
    fun register(entity: Entity) {
        if (!entity.components.include(MATCHER) || ETile in entity.components)
            return
        val transform = entity[ETransform]
        if (viewRef != transform.viewRef || layerRef != transform.layerRef)
            return

        add(entity)
    }

    /** Implements the adding of an specific Entity id after all checks has passed.
     * This is called by the CollisionSystem on entity activation event and should not be called directly
     * If you have to add a entity id directly to a pool, use register that also do the necessary checks before adding.
     *
     * @param entity the Entity to add to the pool
     */
    internal abstract fun add(entity: Entity)

    /** Removes an specified Entity id from the pool.
     * This Usually is called by the CollisionSystem on entity inactivation event but can also be called directly as an API
     *
     * @param entity Entity to remove/unregister from the pool
     */
    abstract fun remove(entity: Entity)

    open fun updateAll(entityIds: BitSet) {
        var i = entityIds.nextSetBit(0)
        while (i >= 0) {
            update(EntitySystem[i])
            i = entityIds.nextSetBit(i + 1)
        }
    }

    /** This is usually called by CollisionSystem in an entity move event and must update the entity in the pool
     * if the entity id has some orientation related store attributes within the specified ContactPool implementation.
     *
     * @param entity the Entity of an entity that has just moved and changed its position in the world
     */
    open fun update(entity: Entity) {
        update(entity.index, entity[ETransform], entity[EContact])
    }

    /** This is usually called by CollisionSystem in an entity move event and must update the entity in the pool
     * if the entity id has some orientation related store attributes within the specified ContactPool implementation.
     *
     * @param entity the Entity of an entity that has just moved and changed its position in the world
     */
    abstract fun update(entityId: Int, transform: ETransform, collision: EContact)

    /** Use this to get an IntIterator of all entity id's that most possibly has a collision within the given region.
     * The efficiency of this depends on an specified implementation and can be different for different needs.
     *
     * @param region The contact or collision region to check collision entity collisions against.
     * @return IntIterator of all entity id's that most possibly has a collision within the given region
     */
    abstract operator fun get(region: Rectangle, entity: Entity): IntIterator

    /** Use this to clear all entity id's form a specified pool instance  */
    abstract fun clear()

    companion object : SystemComponentType<ContactMap>(ContactMap::class) {

        @JvmField internal val MATCHER =
            EntityComponent.ENTITY_COMPONENT_ASPECTS.createAspects(ETransform, EContact)
    }
}