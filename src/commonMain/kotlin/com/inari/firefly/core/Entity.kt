package com.inari.firefly.core

import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.aspect.*
import com.inari.util.collection.AttributesRO
import com.inari.util.collection.AttributesRO.Companion.EMPTY_ATTRIBUTES
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynFloatArray
import com.inari.util.indexed.AbstractIndexed
import kotlin.jvm.JvmField

abstract class EntityComponentType<C : EntityComponent>(
    val typeName: String
) : Aspect {
    val typeAspect: Aspect = Entity.ENTITY_COMPONENT_ASPECTS.createAspect(typeName)
    override val aspectIndex: Int = typeAspect.aspectIndex
    override val aspectName: String = typeAspect.aspectName
    override val aspectType: AspectType = typeAspect.aspectType
    override fun toString() = "EntityComponentType:$aspectName"
}

@ComponentDSL
abstract class EntityComponent protected constructor(
    entityComponentType: EntityComponentType<*>
) : AbstractIndexed(entityComponentType.typeName) {

    var entityIndex: EntityIndex = NULL_COMPONENT_INDEX
        internal set

    internal fun iActivate() = activate()
    internal fun iDeactivate() = deactivate()
    protected open fun activate() {}
    protected open fun deactivate() {}
    abstract fun reset()
    abstract val componentType: EntityComponentType<out EntityComponent>
}

class Entity internal constructor(): Component(Entity), Controlled, AspectAware {

    /** The set of EntityComponent of a specified Entity. EntityComponent are indexed by type for fast accesses */
    internal val components: AspectSet<EntityComponent> = AspectSet.of(ENTITY_COMPONENT_ASPECTS)
    /** The Aspects that reflects the EntityComponent types that are hold by this Entity */
    override val aspects: Aspects = components.aspects

    override fun activate() {
        val iter = components.iterator()
        while (iter.hasNext())
            components.get(iter.next())?.iActivate()
    }
    override fun deactivate() {
        val iter = components.iterator()
        while (iter.hasNext())
            components.get(iter.next())?.iDeactivate()
    }

    fun has(cType: EntityComponentType<*>): Boolean = aspects.contains(cType.typeAspect)
    @Suppress("UNCHECKED_CAST")
    operator fun <C : EntityComponent> get(type: EntityComponentType<C>): C = components.get(type)!! as C
    fun <C : EntityComponent> withComponent(cBuilder: EntityComponentBuilder<C>, configure: (C.() -> Unit)): C =
        cBuilder.builder(this)(configure)
    fun <C : EntityComponent> withComponent(cBuilder: EntityComponentBuilderAdapter<C>, configure: (C.() -> Unit)): C =
        cBuilder.type.builder(this)(configure)

    override fun toString(): String = "${super.toString()} | $aspects"

    companion object : ComponentSystem<Entity>("Entity")  {

        val ENTITY_COMPONENT_ASPECTS = IndexedAspectType("ENTITY_COMPONENT_ASPECTS")
        private val entityListener: ComponentEventListener = { index, eType ->
            if (eType == ComponentEventType.DISPOSED)
                disposeEntity(Entity[index])
        }

        init {
            registerComponentListener(entityListener)
        }

        private val disposedEntities: ArrayDeque<Entity> = ArrayDeque()
        private val disposedComponents: DynArray<ArrayDeque<EntityComponent>> = DynArray.of(100, 100)
        private fun getOrCreate(index: Int): ArrayDeque<EntityComponent> {
            if (!disposedComponents.contains(index))
                disposedComponents[index] = ArrayDeque()
            return disposedComponents[index]!!
        }

        fun createEntityForLaterUse(number: Int) {
            for (i in 0 until number)
                disposedEntities.add(create())
        }

        fun createComponentForLaterUse(number: Int, builder: EntityComponentBuilder<*>) {
            val cache = getOrCreate(builder.aspectIndex)
            for (i in 0 until number)
                cache.add(builder.create())
        }

        @Suppress("UNCHECKED_CAST")
        internal fun <C : EntityComponent> getComponent(builder: EntityComponentBuilder<C>): C {
            val cache = getOrCreate(builder.aspectIndex)
            return if (cache.isEmpty())
                builder.create()
            else
                cache.removeFirst() as C
        }

        private fun disposeEntity(entity: Entity) {
            val iter = entity.components.iterator()
            while (iter.hasNext())
                dispose(entity.components.get(iter.next())!!)

            entity.components.clear()
            entity.disposeIndex()
            disposedEntities.add(entity)
        }

        private fun dispose(entityComponent: EntityComponent) {
            entityComponent.reset()
            getOrCreate(entityComponent.componentType.aspectIndex).add(entityComponent)
        }

        override fun allocateArray(size: Int): Array<Entity?> {
            return arrayOfNulls(size)
        }

        override fun create(): Entity = Entity()
    }
}

abstract class EntityComponentBuilder<C : EntityComponent>(
    typeName: String
) : EntityComponentType<C>(typeName) {

    private fun doBuild(comp: C, configure: C.() -> Unit, entity: Entity): C {
        comp.also(configure)
        comp.entityIndex = entity.index
        entity.components.set(comp.componentType, comp)
        if (entity.active)
            comp.iActivate()
        return comp
    }
    internal fun builder(entity: Entity): (C.() -> Unit) -> C = {
            configure -> doBuild(Entity.getComponent(this), configure, entity)
    }
    abstract fun create(): C

}

interface EntityComponentBuilderAdapter<C : EntityComponent> {
    val type: EntityComponentBuilder<C>
}

class EChild private constructor() : EntityComponent(EChild) {

    @JvmField val parent = CReference(Entity)
    val parentIndex: Int
        get() = parent.targetKey.componentIndex
    @JvmField var zPos: Int = -1

    override fun reset() {
        parent.reset()
        zPos = -1
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EChild>("EChild") {
        override fun create() = EChild()
    }
}

class EMultiplier private constructor() : EntityComponent(EMultiplier) {

    @JvmField var positions: DynFloatArray = DynFloatArray()

    override fun reset() {
        positions.clear()
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EMultiplier>("EMultiplier") {
        override fun create() = EMultiplier()
    }
}

class EAttribute private constructor() : EntityComponent(EAttribute) {

    @JvmField var attributes = EMPTY_ATTRIBUTES

    override fun reset() {
            attributes = EMPTY_ATTRIBUTES
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EAttribute>("EAttribute") {
        override fun create() = EAttribute()
    }
}