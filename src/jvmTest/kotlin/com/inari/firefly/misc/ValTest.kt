package com.inari.firefly.misc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValTest {

    @Test
    fun test1() {
        val test = Test1()
        val v1 = test.testVal1
        val v2 = test.testVal1
        val v3 = test.testVal1

        assertTrue(v1 == v2)
        assertTrue(v3 == v2)
    }

    @Test
    fun test2() {
        val test = Test1()
        val v1 = test.testVal2
        val v2 = test.testVal2
        val v3 = test.testVal2

        assertEquals("1", v1.toString())
        assertEquals("2", v2.toString())
        assertEquals("3", v3.toString())
    }

    @Test
    fun test3() {
        val test = Test1()
        val v1 = test.testVal3
        val v2 = test.testVal3
        val v3 = test.testVal3

        assertEquals("1", v1.toString())
        assertEquals("2", v2.toString())
        assertEquals("3", v3.toString())
    }

    class Test1 {
        private var i: Int = 0
        val testVal1: Int = i++
        val testVal2: Int get() = i++
        val testVal3: Int get() {return i++}
    }
}