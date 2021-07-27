package com.inari.firefly.misc

import com.inari.util.geom.Position
import com.inari.firefly.FFContext
import com.inari.firefly.TestApp
import com.inari.firefly.measureTime
import com.inari.firefly.physics.contact.ContactEvent
import com.inari.util.IntFunction
import com.inari.util.event.Event
import kotlin.test.BeforeTest
import kotlin.test.Test

typealias TestListenerType = (Int, Int, Int) -> Unit

interface TestListenerInterface {
    operator fun invoke(i1: Int, i2: Int, i3: Int)
}

typealias TestListenerTypeObj = (Position, Position, Position) -> Unit

interface TestListenerInterfaceObj {
    operator fun invoke(i1: Position, i2: Position, i3: Position)
}

class TestEvent1(override val eventType: EventType) : Event<TestListenerType>() {

    private var i1: Int = -1
    private var i2: Int = -1
    private var i3: Int = -1

    override fun notify(listener: TestListenerType) =
        listener(i1, i2, i3)

    companion object : EventType("TestEvent1") {
        internal val event = TestEvent1(this)
        fun send(i1: Int, i2: Int, i3: Int) {
            event.i1 = i1
            event.i2 = i2
            event.i3 = i3
            FFContext.notify(event)
        }
    }
}

class TestEvent2(override val eventType: EventType) : Event<TestListenerInterface>() {

    private var i1: Int = -1
    private var i2: Int = -1
    private var i3: Int = -1

    override fun notify(listener: TestListenerInterface) =
        listener(i1, i2, i3)


    companion object : EventType("TestEvent2") {
        internal val event = TestEvent2(this)
        fun send(i1: Int, i2: Int, i3: Int) {
            event.i1 = i1
            event.i2 = i2
            event.i3 = i3
            FFContext.notify(event)
        }
    }
}

class TestEvent3(override val eventType: EventType) : Event<TestListenerTypeObj>() {

    private var i1: Position = Position()
    private var i2: Position = Position()
    private var i3: Position = Position()

    override fun notify(listener: TestListenerTypeObj) =
        listener(i1, i2, i3)

    companion object : EventType("TestEvent3") {
        internal val event = TestEvent3(this)
        fun send(i1: Position, i2: Position, i3: Position) {
            event.i1 = i1
            event.i2 = i2
            event.i3 = i3
            FFContext.notify(event)
        }
    }
}

class TestEvent4(override val eventType: EventType) : Event<TestListenerInterfaceObj>() {

    private var i1: Position = Position()
    private var i2: Position = Position()
    private var i3: Position = Position()

    override fun notify(listener: TestListenerInterfaceObj) =
        listener(i1, i2, i3)

    companion object : EventType("TestEvent4") {
        internal val event = TestEvent4(this)
        fun send(i1: Position, i2: Position, i3: Position) {
            event.i1 = i1
            event.i2 = i2
            event.i3 = i3
            FFContext.notify(event)
        }
    }
}

class EventPerformanceTest {

    @BeforeTest
    fun init() {
        TestApp
    }

    @Test
    fun test0() {
        val intFunctionLambda: IntFunction = { i -> i + 1 }
        val intFunctionInterface: IntFunction =  { p1 -> p1 + 1 }

        measureTime("primitive interface impl", 100_000_000_0) {
            intFunctionInterface(1)
        }

        measureTime("primitive lambda function", 100_000_000_0) {
            intFunctionLambda(1)
        }
    }

    @Test
    fun test1() {

        val testListenerType: TestListenerType = { i1, i2, i3 -> i1 + i2 + i3}
        val testListenerInterface: TestListenerInterface = object : TestListenerInterface {
            override fun invoke(i1: Int, i2: Int, i3: Int) {
                i1 + i2 + i3
            }

        }

        FFContext.registerListener(TestEvent1, testListenerType)
        FFContext.registerListener(TestEvent2, testListenerInterface)

        measureTime("primitive interface listener", 100_000_000) {
            TestEvent2.send(1, 2, 4)
        }

        measureTime("primitive subType listener", 100_000_000) {
            TestEvent1.send(1, 2, 4)
        }


    }

    @Test
    fun test2() {

        FFContext.registerListener(TestEvent1) { i1: Int, i2: Int, i3: Int -> i1 + i2 + i3 }
        FFContext.registerListener(TestEvent2, object : TestListenerInterface {
            override fun invoke(i1: Int, i2: Int, i3: Int) {
                i1 + i2 + i3
            }
        })

        measureTime("primitive subType listener with lambda", 100_000) {
            TestEvent1.send(1, 2, 4)
        }

        measureTime("primitive interface listener with lambda", 100_000) {
            TestEvent2.send(1, 2, 4)
        }
    }

    @Test
    fun test3() {
        val pos1 = Position(1, 2)
        val testListenerType: TestListenerTypeObj = { i1, i2, i3 -> i1.x + i2.x + i3.x}
        val testListenerInterface: TestListenerInterfaceObj = object : TestListenerInterfaceObj {
            override fun invoke(i1: Position, i2: Position, i3: Position) {
                i1.x + i2.x + i3.x
            }
        }

        FFContext.registerListener(TestEvent3, testListenerType)
        FFContext.registerListener(TestEvent4, testListenerInterface)

        measureTime("Obj subType listener", 100_000) {
            TestEvent3.send(pos1, pos1, pos1)
        }

        measureTime("Obj interface listener", 100_000) {
            TestEvent4.send(pos1, pos1, pos1)
        }
    }

    @Test
    fun test4() {
        val pos1 = Position(1, 2)
        FFContext.registerListener(TestEvent3) { i1: Position, i2: Position, i3: Position -> i1.x + i2.x + i3.x }
        FFContext.registerListener(TestEvent4, object : TestListenerInterfaceObj {
            override fun invoke(i1: Position, i2: Position, i3: Position) {
                i1.x + i2.x + i3.x
            }
        })

        measureTime("Obj subType listener with lambda", 100_000) {
            TestEvent3.send(pos1, pos1, pos1)
        }

        measureTime("Obj interface listener with lambda", 100_000) {
            TestEvent4.send(pos1, pos1, pos1)
        }
    }
}