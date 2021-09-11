package com.inari.firefly.game.tile

import com.inari.firefly.BASE_VIEW
import com.inari.firefly.FFContext
import com.inari.firefly.GraphicsMock
import com.inari.firefly.TestApp
import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.game.tiled.TiledTileSetAsset
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.rendering.RenderingSystem
import com.inari.firefly.graphics.tile.TileGridSystem
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.firefly.physics.animation.AnimationSystem
import com.inari.util.geom.BitMask
import kotlin.test.*

class TestTiledTileSetAsset {

    @Test
    fun testLoad_Activate_Deactivate() {
        TestApp
        RenderingSystem
        AssetSystem
        EntitySystem
        ViewSystem
        TileMapSystem
        TileGridSystem
        AnimationSystem


        val tileSetAssetId = TiledTileSetAsset.build {
            name = "testTileSet"
            resourceName = "tiles/outline_full.json"
        }

        assertTrue { AssetSystem.assets.contains("testTileSet") }

        // create tilemap for the tile set
        val tileMapId = TileMap.build {
            view(FFContext[View, BASE_VIEW])
            withLayer {
                withTileSet {
                    tileSetAsset(tileSetAssetId)
                    codeOffset = 1
                }
            }
        }

        TileMapSystem.loadTileMap(tileMapId)

        // check all expected assets and components are created correctly
        val tiledTileSetAsset = FFContext[TiledTileSetAsset, tileSetAssetId]
        assertNotNull(tiledTileSetAsset)
        val textureAssetName = "texture_${tiledTileSetAsset.name}"
        assertTrue { AssetSystem.assets.contains( textureAssetName ) }
        val textureAsset = FFContext[TextureAsset, textureAssetName]
        assertNotNull(textureAsset)
        assertTrue { textureAsset.loaded() }
        assertEquals("tiles/outline_16_16.png", GraphicsMock._loadedAssets[0])

        assertTrue { TileMapSystem.tileSets.contains("outline_full") }
        val tileSet = TileMapSystem.tileSets["outline_full"]
        assertEquals("40", tileSet.tileTemplates.size.toString())

        // test some tile templates
        val tile1 = tileSet.tileTemplates[0]!!
        assertEquals("outline_full_terrain_quad:full_0:0", tile1.name)
        assertTrue(TileSizeType.FULL in tile1.aspects)
        assertEquals( "TERRAIN_SOLID", tile1.material.toString())
        assertEquals( "QUAD", tile1.contactType.toString())
        assertNull(tile1.contactMask)

        val tile2 = tileSet.tileTemplates[3]!!
        assertEquals("outline_full_terrain_slope:quarterto:se:h_3:0", tile2.name)
        assertTrue(TileOrientation.HORIZONTAL in tile2.aspects)
        assertTrue(TileSizeType.QUARTER_TO in tile2.aspects)
        assertTrue(TileDirection.SOUTH_EAST in tile2.aspects)
        //assertEquals("Aspects [aspectType=TILE_ASPECT_GROUP {QUARTER_TO, SOUTH_EAST, HORIZONTAL}]", tile2.aspects.toString())
        assertEquals( "TERRAIN_SOLID", tile2.material.toString())
        assertEquals( "SLOPE", tile2.contactType.toString())
        assertNotNull(tile2.contactMask)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "0000000000000011\n" +
                    "0000000000001111\n" +
                    "0000000000111111\n" +
                    "0000000011111111\n" +
                    "0000001111111111\n" +
                    "0000111111111111\n" +
                    "0011111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111]",
            tile2.contactMask.toString())

        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "0000000000000000\n" +
                    "0000000000000000\n" +
                    "0000000000000000\n" +
                    "0000000000000000\n" +
                    "0000000000000000\n" +
                    "0000000000000000\n" +
                    "0000000000000000\n" +
                    "0000000000000000\n" +
                    "0000000110000000\n" +
                    "0000001111000000\n" +
                    "0000011111100000\n" +
                    "0000111111110000\n" +
                    "0001111111111000\n" +
                    "0011111111111100\n" +
                    "0111111111111110\n" +
                    "1111111111111111]",
            tileSet.tileTemplates[7]!!.contactMask.toString())
        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "0000000010000000\n" +
                    "0000000110000000\n" +
                    "0000000111000000\n" +
                    "0000001111000000\n" +
                    "0000001111100000\n" +
                    "0000011111100000\n" +
                    "0000011111110000\n" +
                    "0000111111110000\n" +
                    "0000111111111000\n" +
                    "0001111111111000\n" +
                    "0001111111111100\n" +
                    "0011111111111100\n" +
                    "0011111111111110\n" +
                    "0111111111111110\n" +
                    "0111111111111111\n" +
                    "1111111111111111]",
            tileSet.tileTemplates[8]!!.contactMask.toString())
        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "1000000000000000\n" +
                    "1100000000000000\n" +
                    "1110000000000000\n" +
                    "1111000000000000\n" +
                    "1111100000000000\n" +
                    "1111110000000000\n" +
                    "1111111000000000\n" +
                    "1111111100000000\n" +
                    "1111111100000000\n" +
                    "1111111000000000\n" +
                    "1111110000000000\n" +
                    "1111100000000000\n" +
                    "1111000000000000\n" +
                    "1110000000000000\n" +
                    "1100000000000000\n" +
                    "1000000000000000]",
            tileSet.tileTemplates[27]!!.contactMask.toString())
        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "1100000000000000\n" +
                    "1111000000000000\n" +
                    "1111110000000000\n" +
                    "1111111100000000\n" +
                    "1111111111000000\n" +
                    "1111111111110000\n" +
                    "1111111111111100\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111100\n" +
                    "1111111111110000\n" +
                    "1111111111000000\n" +
                    "1111111100000000\n" +
                    "1111110000000000\n" +
                    "1111000000000000\n" +
                    "1100000000000000]",
            tileSet.tileTemplates[28]!!.contactMask.toString())

        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "0000000000000001\n" +
                    "0000000000000011\n" +
                    "0000000000000111\n" +
                    "0000000000001111\n" +
                    "0000000000011111\n" +
                    "0000000000111111\n" +
                    "0000000001111111\n" +
                    "0000000011111111\n" +
                    "0000000011111111\n" +
                    "0000000001111111\n" +
                    "0000000000111111\n" +
                    "0000000000011111\n" +
                    "0000000000001111\n" +
                    "0000000000000111\n" +
                    "0000000000000011\n" +
                    "0000000000000001]",
            tileSet.tileTemplates[37]!!.contactMask.toString())
        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "0000000000000011\n" +
                    "0000000000001111\n" +
                    "0000000000111111\n" +
                    "0000000011111111\n" +
                    "0000001111111111\n" +
                    "0000111111111111\n" +
                    "0011111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "0011111111111111\n" +
                    "0000111111111111\n" +
                    "0000001111111111\n" +
                    "0000000011111111\n" +
                    "0000000000111111\n" +
                    "0000000000001111\n" +
                    "0000000000000011]",
            tileSet.tileTemplates[38]!!.contactMask.toString())

        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "0000000000000011\n" +
                    "0000000000001111\n" +
                    "0000000000111111\n" +
                    "0000000011111111\n" +
                    "0000001111111111\n" +
                    "0000111111111111\n" +
                    "0011111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "0011111111111111\n" +
                    "0000111111111111\n" +
                    "0000001111111111\n" +
                    "0000000011111111\n" +
                    "0000000000111111\n" +
                    "0000000000001111\n" +
                    "0000000000000011]",
            tileSet.tileTemplates[38]!!.contactMask.toString())

        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "0000001111000000\n" +
                    "0000111111110000\n" +
                    "0001111111111000\n" +
                    "0011111111111100\n" +
                    "0111111111111110\n" +
                    "0111111111111110\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "0111111111111110\n" +
                    "0111111111111110\n" +
                    "0011111111111100\n" +
                    "0001111111111000\n" +
                    "0000111111110000\n" +
                    "0000001111000000]",
            tileSet.tileTemplates[10]!!.contactMask.toString())

        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "0000000110000000\n" +
                    "0000001111000000\n" +
                    "0000011111100000\n" +
                    "0000111111110000\n" +
                    "0001111111111000\n" +
                    "0011111111111100\n" +
                    "0111111111111110\n" +
                    "1111111111111111\n" +
                    "1111111111111111\n" +
                    "0111111111111110\n" +
                    "0011111111111100\n" +
                    "0001111111111000\n" +
                    "0000111111110000\n" +
                    "0000011111100000\n" +
                    "0000001111000000\n" +
                    "0000000110000000]",
            tileSet.tileTemplates[20]!!.contactMask.toString())



        // test tile map activation
        assertTrue{ TileMapSystem.getTileEntityRef(1) == -1 }
        FFContext.activate(TileMap, tileMapId)

        var entityRefId = TileMapSystem.getTileEntityRef(1)
        assertTrue( entityRefId >= 0)
        var entity = FFContext[Entity, entityRefId]
        assertEquals("outline_full_terrain_quad:full_0:0_v0l0", entity.name)

        entityRefId = TileMapSystem.getTileEntityRef(11)
        assertTrue( entityRefId >= 0)
        entity = FFContext[Entity, entityRefId]
        assertEquals("outline_full_terrain_circle:full_0:1_v0l0", entity.name)
    }

    @Test
    fun checkScanlineDistance() {
        val scanlineVert = BitMask(0, 0, 1, 16)
        var firstSet  = -1
        var lastSet = -1
        var i = 0
        while (i < 16) {
            if (scanlineVert.getBit(0, i)) {
                lastSet = i
                if (firstSet > 0)
                    firstSet = i
            }
            i++
        }
        assertEquals(-1, firstSet)
        assertEquals(-1, lastSet)

        scanlineVert.setBit(0, 2)
        firstSet  = -1
        lastSet = -1
        i = 0
        while (i < 16) {
            if (scanlineVert.getBit(0, i)) {
                lastSet = i
                if (firstSet < 0)
                    firstSet = i
            }
            i++
        }
        assertEquals(2, firstSet)
        assertEquals(2, lastSet)

        scanlineVert.setBit(0, 4)
        firstSet  = -1
        lastSet = -1
        i = 0
        while (i < 16) {
            if (scanlineVert.getBit(0, i)) {
                lastSet = i
                if (firstSet < 0)
                    firstSet = i
            }
            i++
        }
        assertEquals(2, firstSet)
        assertEquals(4, lastSet)

    }

}