package com.inari.util

import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.sprite.ESprite
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinTests {

    @Test
    fun testAspects() {
        for (i in 1 .. 100000)
                Finalized(i)
        System.gc()
        Thread.sleep(1000)
    }

    @Test
    fun testAccessorEquality() {
        val entity = Entity {
            withComponent(ESprite) {
                spriteRef(1)
            }
        }


        val accessorProvider1 = ESprite.PropertyAccessor.SPRITE_INDEX
        val accessorProvider2 = ESprite.PropertyAccessor.SPRITE_INDEX
        val accessor1 = accessorProvider1(entity.componentIndex)
        val accessor2 = accessorProvider2(entity.componentIndex)

        assertEquals(accessorProvider1, accessorProvider2)
        assertEquals(accessor1, accessor2)
    }
}

class Finalized(@JvmField val num: Int) {



    protected fun finalize() {
        //println("***************** finalized $num")
    }

    fun test() {
        val testArray: TestArray<String> = TestArray<String> {
            arrayOfNulls(it)
        }
        val t = testArray.get(0)
        testArray.blabla(0)
        val i = testArray.x
    }
}

class TestArray<T>(val arrayAllocation: (Int) -> Array<T?>) {

    var v0 = 1
    inline var x: Int
        get() = v0
        set(value) { v0 = value }

    @JvmField internal var array: Array<T?> = arrayAllocation(2)

    fun get(index: Int): T? = array[index]
    fun apply(f: (T?) -> Unit) {
        f(array[0])
    }
    fun blabla(index: Int): String {
        return ""
    }
}