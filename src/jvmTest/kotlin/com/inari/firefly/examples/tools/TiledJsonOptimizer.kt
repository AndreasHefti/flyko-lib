package com.inari.firefly.examples.tools

import com.inari.firefly.game.json.TileSetJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import kotlin.test.assertEquals

object TiledJsonOptimizer {

    private val JSONMapper = Moshi
        .Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @JvmStatic fun main(arg: Array<String>) {
        val path = System.getProperty("user.dir")
        val file = File("C:/Users/anhef/IdeaProjects/flyko-lib/src/jvmTest/resources/tiled_templates/full_tileset_16.json")
        val jsonFileText = file
            .readText()

        val tileSetJSON = JSONMapper.adapter(TileSetJson::class.java).fromJson(jsonFileText)

        val jsonText = JSONMapper
            .adapter(TileSetJson::class.java)
            .toJson(tileSetJSON)

        //file.writeText(jsonText)

        assertEquals(jsonFileText, jsonText)
    }

}