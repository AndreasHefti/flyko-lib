package com.inari.firefly.asset

import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponentSubType


class TestAsset private constructor(
        var Param1: String = "",
        var Param2: Float = 0.0f
) : Asset() {

    var dependsOn =
        ComponentRefResolver(Asset) { index-> dependingRef = setIfNotInitialized(index, "DependsOn") }

    override var instanceId: Int = -1
    override fun instanceId(index: Int): Int = instanceId

    override fun load() {
        instanceId = 1
    }

    override fun unload() {
        instanceId = -1
    }

    override fun toString(): String {
        return "TestAsset(name='$name', " +
            "Param1='$Param1', " +
            "Param2=$Param2, " +
            "instanceId=$instanceId)" +
            " dependsOn=$dependingRef"
    }

    companion object : SystemComponentSubType<Asset, TestAsset>(Asset, TestAsset::class) {
        override fun createEmpty(): TestAsset = TestAsset()
    }
}