package com.inari.firefly.game.world

import com.inari.firefly.TestApp
import com.inari.firefly.core.Component
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.game.*
import com.inari.firefly.game.room.TiledRoomConversionTask
import com.inari.firefly.game.room.TiledTileSetConversionTask
import com.inari.util.collection.Attributes
import kotlin.test.*


class JsonConverterTest {

    @BeforeTest
    fun init() {
        TestApp
        ComponentSystem.clearSystems()
    }

    @Test
    fun testConvertTiledTileSetToJsonTileSet() {
        val attributes = Attributes() +
                ( ATTR_RESOURCE to "tiled_tileset_example/tiled_tileset.json" ) +
                ( ATTR_TARGET_FILE to "C:/Users/anhef/IdeaProjects/flyko-lib/src/jvmTest/resources/json_example/tileset0.json")

        TiledTileSetConversionTask(Component.NO_COMPONENT_KEY, attributes)
    }

    @Test
    fun testConvertTiledRoom1ToJson() {
        val attributes = Attributes() +
                ( ATTR_RESOURCE to "tiled_map_example/example_map1.json" ) +
                ( ATTR_TARGET_FILE to "C:/Users/anhef/IdeaProjects/flyko-lib/src/jvmTest/resources/json_example/map1.json") +
                ( ATTR_TILESET_OVERRIDE_PREFIX + "blueGrid1616" to "json_example/tileset0.json")

        TiledRoomConversionTask(Component.NO_COMPONENT_KEY, attributes)
    }

    @Test
    fun testConvertTiledRoom2ToJson() {
        val attributes = Attributes() +
                ( ATTR_RESOURCE to "tiled_map_example/example_map2.json" ) +
                ( ATTR_TARGET_FILE to "C:/Users/anhef/IdeaProjects/flyko-lib/src/jvmTest/resources/json_example/map2.json") +
                ( ATTR_TILESET_OVERRIDE_PREFIX + "blueGrid1616" to "json_example/tileset0.json")

        TiledRoomConversionTask(Component.NO_COMPONENT_KEY, attributes)
    }
}