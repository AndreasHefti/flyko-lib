package com.inari.firefly.misc

import com.inari.firefly.measureTime
import com.inari.util.collection.DynArray
import kotlin.test.Ignore
import kotlin.test.Test

class DynArrayExtensionTest {

    //@Test
    fun testPerformance() {

        val dynArray: DynArray<Int> = DynArray.of(100000)
        for(i: Int in 1..100000)
            dynArray.add(i)

        var amount = 0
        measureTime("imperative loop", 10000) {
            val size = dynArray.capacity
            if (size > 0) {
                var i = 0
                while (i < size) {
                    val value = dynArray[i++] ?: continue
                    if (value.toInt() % 2 == 0)
                        continue

                    amount += value.toInt()
                }
            }
        }

        val exp: (Int) -> Unit = { value -> if ( value.toInt() % 2 == 0) amount += value.toInt() }
        amount = 0
        measureTime("conventional forEach with predicate and expression in one function", 10000) {
            dynArray.forEach { value -> if (value.toInt() % 2 == 0) amount += value.toInt() }
        }

    }

}