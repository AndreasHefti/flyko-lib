package com.inari.firefly.misc

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType

class TestComponent private constructor (
    var Param1: String,
    override var Param2: Int
) : SystemComponent(TestComponent::class.simpleName!!), ITestComponent {

    private constructor() : this("Param1", 0)

    override fun componentType() = Companion

    companion object : SystemComponentSingleType<TestComponent>(TestComponent::class) {
        override fun createEmpty(): TestComponent = TestComponent()
    }

}