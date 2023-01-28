package com.inari.util.aspect

import com.inari.util.collection.DynArray
import com.inari.util.indexed.AbstractIndexed

interface Aspect {
    val aspectName: String
    val aspectType: AspectType
    val aspectIndex: Int
}

interface AspectAware {
    val aspects: Aspects
}

interface AspectType {
    val name: String
    fun createAspects(): Aspects
    fun createAspects(vararg aspects: Aspect): Aspects
    fun createAspect(name: String): Aspect
    fun typeCheck(aspect: Aspect): Boolean
    operator fun get(name: String): Aspect?
    operator fun get(index: Int): Aspect?

}

class IndexedAspectType(
    override val name: String
) : AspectType {

    private val aspects: DynArray<Aspect> = DynArray.of(10, 10)

    override fun createAspects(): Aspects =
        Aspects(this)

    override fun createAspects(vararg aspects: Aspect): Aspects {
        val result = Aspects(this)
        for (aspect in aspects)
            result + aspect
        return result
    }

    override operator fun get(name: String): Aspect? {
        for (aspect in aspects)
            if (name == aspect.aspectName)
                return aspect
        return null
    }

    override operator fun get(index: Int): Aspect? =
        aspects[index]

    override fun typeCheck(aspect: Aspect) =
        aspect.aspectType === this


    override fun createAspect(name: String): Aspect {
        val aspect = get(name)
        if (aspect != null)
            return aspect

        val newAspect = IndexedAspect(name, this)
        aspects[newAspect.aspectIndex] = newAspect
        return newAspect
    }

    class IndexedAspect constructor(
        override val aspectName: String,
        override val aspectType: IndexedAspectType
    ) : AbstractIndexed(aspectType.name), Aspect {
        override val aspectIndex: Int
            get() = index

        override fun toString(): String =
                "$aspectName:$aspectType"
    }

    override fun toString() =
        name

}

class AspectTypeMismatchException(val msg: String) : RuntimeException(msg)

