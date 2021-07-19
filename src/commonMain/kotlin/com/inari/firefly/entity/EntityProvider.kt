package com.inari.firefly.entity

import com.inari.util.collection.DynArray

object EntityProvider {

    private val disposedEntities: ArrayDeque<Entity> = ArrayDeque()
    private val disposedComponents: DynArray<ArrayDeque<EntityComponent>> = DynArray.of(20, 10)

    fun createEntityForLaterUse(number: Int) {
        for (i in 0 until number)
            disposedEntities.add(Entity.createEmpty())
    }

    fun createComponentForLaterUse(number: Int, builder: EntityComponentBuilder<*>) {
        val cache = getOrCreate(builder.aspectIndex)
        for (i in 0 until number)
            cache.add(builder.create())
    }

    @Suppress("UNCHECKED_CAST")
    fun <C : EntityComponent> getComponent(builder: EntityComponentBuilder<C>): C {
        val cache = getOrCreate(builder.aspectIndex)
        return if (cache.isEmpty())
            builder.create()
        else
            cache.removeFirst() as C
    }

    fun get(): Entity {
        return if (disposedEntities.isEmpty())
            Entity()
        else
            disposedEntities.removeFirst().restore()
    }

    fun dispose(entity: Entity) {
        val entityId = entity.index
        if (EntitySystem.entities.isActive(entityId))
            throw IllegalStateException("Entity: $entityId is still active and cannot be disposed")

        entity.reset()
        disposedEntities.add(entity)
    }

    fun dispose(entityComponent: EntityComponent) {
        entityComponent.internalReset()
        getOrCreate(entityComponent.index).add(entityComponent)
    }

    private fun getOrCreate(index: Int): ArrayDeque<EntityComponent> {
        if (!disposedComponents.contains(index))
            disposedComponents[index] = ArrayDeque()
        return disposedComponents[index]!!
    }

}