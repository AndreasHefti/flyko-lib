package com.inari.firefly.core

import com.inari.firefly.TestApp
import com.inari.util.NO_NAME
import kotlin.test.*

class AssetTest {

    @BeforeTest
    fun init() {
        TestApp
        Asset.clearSystem()
    }

    @Test
    fun assetCreation() {

        assertFalse(Asset.hasComponents)
        val emptyAssetId = TestAsset.build {}
        assertTrue(Asset.hasComponents)

        assertEquals(
            "CKey([[NO_NAME]], Asset, 0)",
            emptyAssetId.toString()
        )

        val emptyAsset = Asset[emptyAssetId]
        assertNotNull(emptyAsset)
        assertEquals(
            "TestAsset(name='[[NO_NAME]]', Param1='', Param2=0.0, assetIndex=-1)",
            emptyAsset.toString()
        )

        try {
            Asset[NO_NAME]
            fail("Exception expected here")
        } catch (e: Exception) {
            assertEquals("No component for name: [[NO_NAME]] found on system: Asset", e.message)
        }

        val assetId = TestAsset.build {
            name = "testName"
            Param1 = "param1"
            Param2 = 1.45f
        }

        assertNotNull(assetId)
        val assetById = Asset[assetId]
        val assetByName = Asset["testName"]
        assertEquals(assetById, assetByName)
    }



    @Test
    fun simpleLifeCycle() {

        val testEvents = StringBuilder()
        val assetListener: ComponentEventListener =  { _, type ->
            testEvents.append("|").append(type.toString())
        }

        Asset.registerComponentListener(assetListener)
        assertEquals("", testEvents.toString())

        val assetId = TestAsset.build {
            name = "testName"
            Param1 = "param1"
            Param2 = 1.45f
        }

        assertEquals(
            "CKey(testName, Asset, 0)",
            assetId.toString()
        )
        assertEquals(
            "|INITIALIZED",
            testEvents.toString()
        )

        Asset.activate(assetId)
        assertEquals(
            "|INITIALIZED|LOADED|ACTIVATED",
            testEvents.toString()
        )
        Asset.deactivate(assetId)
        assertEquals(
            "|INITIALIZED|LOADED|ACTIVATED|DEACTIVATED",
            testEvents.toString()
        )
        Asset.activate(assetId)
        assertEquals(
            "|INITIALIZED|LOADED|ACTIVATED|DEACTIVATED|ACTIVATED",
            testEvents.toString()
        )
        Asset.delete(assetId)
        assertEquals(
            "|INITIALIZED|LOADED|ACTIVATED|DEACTIVATED|ACTIVATED|DEACTIVATED|DISPOSED|DELETED",
            testEvents.toString()
        )
    }

    @Test
    fun lifeCycleWithDependentAssets() {
        val testEvents = StringBuilder()
        val assetListener: ComponentEventListener =  { index, type ->
            testEvents.append("|index=").append(index).append(":").append(Asset[index].name).append(":").append(type)
        }
        Asset.registerComponentListener(assetListener )
        assertEquals("", testEvents.toString())

        val asset1 = TestAsset.build {
            name = "parentAsset"
            Param1 = "parent"
            Param2 = 1.45f

            withChild(TestAsset) {
                name = "childAsset"
                Param1 = "child"
                Param2 = 1.45f
            }
        }

        assertEquals(
            "|index=1:childAsset:INITIALIZED|index=0:parentAsset:INITIALIZED",
            testEvents.toString()
        )
        assertEquals(
            "TestAsset(name='childAsset', Param1='child', Param2=1.45, assetIndex=-1)",
            Asset["childAsset"].toString()
        )

        Asset.activate("childAsset")
        assertEquals(
            "|index=1:childAsset:INITIALIZED" +
                    "|index=0:parentAsset:INITIALIZED" +
                    "|index=0:parentAsset:LOADED" +
                    "|index=1:childAsset:LOADED" +
                    "|index=0:parentAsset:ACTIVATED" +
                    "|index=1:childAsset:ACTIVATED",
            testEvents.toString()
        )
        Asset.dispose(asset1)
        assertEquals(
            "|index=1:childAsset:INITIALIZED" +
                    "|index=0:parentAsset:INITIALIZED" +
                    "|index=0:parentAsset:LOADED" +
                    "|index=1:childAsset:LOADED" +
                    "|index=0:parentAsset:ACTIVATED" +
                    "|index=1:childAsset:ACTIVATED" +
                    "|index=1:childAsset:DEACTIVATED" +
                    "|index=0:parentAsset:DEACTIVATED" +
                    "|index=1:childAsset:DISPOSED" +
                    "|index=0:parentAsset:DISPOSED",
            testEvents.toString()
        )

    }
}

class TestAsset private constructor(
    var Param1: String = "",
    var Param2: Float = 0.0f
) : Asset() {

    override fun load() {
        assetIndex = 0
    }

    override fun dispose() {
        assetIndex = -1
    }

    override fun toString(): String {
        return "TestAsset(name='$name', " +
                "Param1='$Param1', " +
                "Param2=$Param2, " +
                "assetIndex=$assetIndex)"
    }

    companion object :  ComponentSubTypeSystem<Asset, TestAsset>(Asset, "TestAsset") {
        override fun create() = TestAsset()
    }
}