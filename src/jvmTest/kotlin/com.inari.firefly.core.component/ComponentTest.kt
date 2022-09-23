package com.inari.firefly.core.component

import com.inari.firefly.core.*
import com.inari.firefly.graphics.view.View
import com.inari.util.NO_NAME
import kotlin.test.*

class ComponentTest {

    @Test
    fun testLifecycle() {


        val compListener: ComponentEventListener = { index, eType ->
            println("*** $index *** $eType")
        }
        TestComponent.registerComponentListener(compListener)

        val key = TestComponent.createKey("testName")
        assertEquals("CKey(testName, TestComponent, -1)", key.toString())

        val id = TestComponent.build {
            name = "testName"
            test1 = "1"
            test2 = "2"
        }

        assertEquals("CKey(testName, TestComponent, 0)", id.toString())
        assertEquals("CKey(testName, TestComponent, 0)", key.toString())
        assertEquals(key, id)

        val comp1 = TestComponent[id]
        val comp2 = TestComponent[key]

        assertEquals(comp1, comp2)
        assertEquals("testName", comp1.name)
        assertEquals("testName", comp2.name)

        try {
            TestComponent.build {
                name = "testName"
                test1 = "1"
                test2 = "2"
            }
            fail("Exception expected here")
        } catch (e:Exception) {
            assertEquals("Key with same name already exists", e.message)
        }

        val newCompKey = TestComponent.build {
            name = "testName2"
            test1 = "1"
            test2 = "2"
        }

        val comp = TestComponent[newCompKey]
        assertNotNull(comp)
        assertTrue(comp.initialized)
        assertFalse(comp.loaded)
        assertFalse(comp.active)
        TestComponent.load(newCompKey)
        assertTrue(comp.initialized)
        assertTrue(comp.loaded)
        assertFalse(comp.active)
        TestComponent.activate(newCompKey)
        assertTrue(comp.initialized)
        assertTrue(comp.loaded)
        assertTrue(comp.active)
        TestComponent.deactivate(newCompKey)
        assertTrue(comp.initialized)
        assertTrue(comp.loaded)
        assertFalse(comp.active)
        TestComponent.dispose(newCompKey)
        assertTrue(comp.initialized)
        assertFalse(comp.loaded)
        assertFalse(comp.active)

        TestComponent.activate(newCompKey)
        assertTrue(comp.initialized)
        assertTrue(comp.loaded)
        assertTrue(comp.active)
        TestComponent.dispose(newCompKey)
        assertTrue(comp.initialized)
        assertFalse(comp.loaded)
        assertFalse(comp.active)

    }

    @Test
    fun testParentChildRefs() {

        val parentKey = TestParent.build {
            name = "parent1"
            ptest1 = "ptest1"
            withChild(TestChild, ChildLifeCyclePolicy.ACTIVATE) {
                name = "child1"
                cest1 = "cest1"
                parent("parent1")
            }
            withChild(TestChild, ChildLifeCyclePolicy.LOAD) {
                name = "child2"
                cest1 = "cest2"
                parent("parent1")
            }
        }

        println(TestParent)
        println(TestChild)

        val parent1 = TestParent["parent1"]
        val child1 = TestChild["child1"]
        val child2 = TestChild["child2"]

        assertTrue(parent1.initialized)
        assertTrue(child1.initialized)
        assertTrue(child2.initialized)
        assertFalse(parent1.loaded)
        assertFalse(child1.loaded)
        assertFalse(child2.loaded)

        // load parent shall also load the children after parent is loaded
        TestParent.load("parent1")

        assertTrue(parent1.initialized)
        assertTrue(child1.initialized)
        assertTrue(child2.initialized)
        assertTrue(parent1.loaded)
        assertTrue(child1.loaded)
        assertTrue(child2.loaded)

        // dispose one child should not trigger parent (or other children) to dispose too
        TestChild.dispose("child1")

        assertTrue(parent1.initialized)
        assertTrue(child1.initialized)
        assertTrue(child2.initialized)
        assertTrue(parent1.loaded)
        assertFalse(child1.loaded)
        assertTrue(child2.loaded)

        // activate parent should trigger to activate children after parent is loaded
        // and also load unloaded children first
        TestParent.activate("parent1")

        assertTrue(parent1.initialized)
        assertTrue(child1.initialized)
        assertTrue(child2.initialized)
        assertTrue(parent1.loaded)
        assertTrue(child1.loaded)
        assertTrue(child2.loaded)
        assertTrue(parent1.active)
        assertTrue(child1.active)
        assertFalse(child2.active) // because of ChildLifeCyclePolicy.LOAD
    }

    class TestComponent : Component(TestComponent) {

        @JvmField var test1: String = NO_NAME
        @JvmField var test2: String = NO_NAME

        override val componentType = Companion
        companion object : ComponentSystem<TestComponent>("TestComponent") {
            override fun allocateArray(size: Int): Array<TestComponent?> = arrayOfNulls(size)
            override fun create() = TestComponent()
        }

    }

    class TestParent : Component(TestParent) {
        @JvmField var ptest1: String = NO_NAME
        @JvmField var ptest2: String = NO_NAME


        override val componentType = Companion
        companion object : ComponentSystem<TestParent>("TestParent") {
            override fun allocateArray(size: Int): Array<TestParent?> = arrayOfNulls(size)
            override fun create() = TestParent()
        }
    }

    class TestChild : Component(TestChild) {

        @JvmField var parent = CReference(TestParent)
        @JvmField var cest1: String = NO_NAME
        @JvmField var cest2: String = NO_NAME

        override val componentType = Companion
        companion object : ComponentSystem<TestChild>("TestChild") {
            override fun allocateArray(size: Int): Array<TestChild?> = arrayOfNulls(size)
            override fun create() = TestChild()
        }

    }

}

