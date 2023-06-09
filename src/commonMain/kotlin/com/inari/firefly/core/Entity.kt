package com.inari.firefly.core

import com.inari.firefly.core.Entity.Companion.ENTITY_COMPONENT_BUILDER
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.ZERO_INT
import com.inari.util.aspect.*
import com.inari.util.collection.AttributesRO.Companion.EMPTY_ATTRIBUTES
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynFloatArray
import com.inari.util.indexed.AbstractIndexed
import kotlin.jvm.JvmField

abstract class EntityComponentType<C : EntityComponent>(
    val typeName: String
) : Aspect {
    @JvmField val typeAspect: Aspect = Entity.ENTITY_COMPONENT_ASPECTS.createAspect(typeName)
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
    internal fun iDispose()  {
        reset()
        ENTITY_COMPONENT_BUILDER[this.componentType.aspectIndex]
            ?.components?.remove(this.entityIndex)
        this.entityIndex = NULL_COMPONENT_INDEX
    }
    protected open fun activate() {}
    protected open fun deactivate() {}
    protected abstract fun reset()
    abstract val componentType: EntityComponentType<out EntityComponent>
}

class Entity internal constructor(): Component(Entity), Controlled, AspectAware {

    /** The Aspects that reflects the EntityComponent types that are hold by this Entity */
    override val aspects: Aspects = ENTITY_COMPONENT_ASPECTS.createAspects()  //components.aspects
    inline operator fun contains(cType: EntityComponentType<*>) = aspects.contains(cType.typeAspect)
    inline fun include(aspects: Aspects) = this.aspects.include(aspects)
    inline fun exclude(aspects: Aspects) = this.aspects.exclude(aspects)

    override fun activate() {
        var i = aspects.bitSet.nextSetBit(0)
        while (i >= ZERO_INT) {
            ENTITY_COMPONENT_BUILDER[i]?.components?.get(index)?.iActivate()
            i = aspects.bitSet.nextSetBit(i + 1)
        }
    }
    override fun deactivate() {
        var i = aspects.bitSet.nextSetBit(0)
        while (i >= ZERO_INT) {
            ENTITY_COMPONENT_BUILDER[i]?.components?.get(index)?.iDeactivate()
            i = aspects.bitSet.nextSetBit(i + 1)
        }
    }

    fun <C : EntityComponent> withComponent(cBuilder: EntityComponentBuilder<C>, configure: (C.() -> Unit)): C =
        cBuilder.builder(this)(configure)
    fun <C : EntityComponent> withComponent(cBuilder: EntityComponentBuilderAdapter<C>, configure: (C.() -> Unit)): C =
        cBuilder.type.builder(this)(configure)

    override fun toString(): String = "${super.toString()} | $aspects"

    companion object : ComponentSystem<Entity>("Entity")  {
        override fun allocateArray(size: Int): Array<Entity?> = arrayOfNulls(size)
        override fun create() = Entity()

        override fun setMinCapacity(c: Int) {
            super.setMinCapacity(c)
            ENTITY_COMPONENT_BUILDER.forEach {
                it.components.ensureCapacity(c)
            }
        }

        override fun optimize() {
            super.optimize()
            ENTITY_COMPONENT_BUILDER.forEach {
                it.components.trim()
            }
        }

        @JvmField internal val ENTITY_COMPONENT_BUILDER = DynArray.of<EntityComponentBuilder<*>>()
        @JvmField val ENTITY_COMPONENT_ASPECTS = IndexedAspectType("ENTITY_COMPONENT_ASPECTS")
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
            var i = entity.aspects.bitSet.nextSetBit(0)
            while (i >= ZERO_INT) {
                val comp = ENTITY_COMPONENT_BUILDER[i]?.components?.get(entity.index)
                if (comp != null)
                    dispose(comp)
                i = entity.aspects.bitSet.nextSetBit(i + 1)
            }

            entity.aspects.clear()
            entity.disposeIndex()
            disposedEntities.add(entity)
        }

        private fun dispose(entityComponent: EntityComponent) {
            entityComponent.iDispose()
            getOrCreate(entityComponent.componentType.aspectIndex).add(entityComponent)
        }
    }
}

abstract class EntityComponentBuilder<C : EntityComponent>(typeName: String) : EntityComponentType<C>(typeName) {

    init {
        @Suppress("LeakingThis")
        ENTITY_COMPONENT_BUILDER[aspectIndex] = this
    }

    @Suppress("LeakingThis")
    @JvmField val components = allocateArray()
    inline operator fun get(index: EntityIndex): C = components.array[index]!!
    inline operator fun get(entityName: String): C = components.array[Entity[entityName].index]!!
    inline operator fun get(entity: Entity): C = components.array[entity.index]!!
    inline operator fun contains(index: Int): Boolean = index < components.array.size && components.array[index] != null
    inline fun getIfExists(index: Int): C? =  if (index in this) this[index] else null

    private fun doBuild(comp: C, configure: C.() -> Unit, entity: Entity): C {
        configure(comp)
        comp.entityIndex = entity.index
        components[entity.index] = comp
        entity.aspects[this] = true
        if (entity.active)
            comp.iActivate()
        return comp
    }
    internal fun builder(entity: Entity): (C.() -> Unit) -> C = {
            configure -> doBuild(Entity.getComponent(this), configure, entity)
    }

    protected abstract fun allocateArray(): DynArray<C>
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
        override fun allocateArray() = DynArray.of<EChild>()
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
        override fun allocateArray() = DynArray.of<EMultiplier>()
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
        override fun allocateArray() = DynArray.of<EAttribute>()
        override fun create() = EAttribute()
    }
}