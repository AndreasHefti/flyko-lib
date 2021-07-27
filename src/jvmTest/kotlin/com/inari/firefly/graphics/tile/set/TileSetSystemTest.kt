package com.inari.firefly.graphics.tile.set

import com.inari.firefly.*
import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.sprite.SpriteSetAsset
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.firefly.physics.animation.AnimationSystem
import com.inari.util.geom.BitMask
import com.inari.util.graphics.IColor.Companion.WHITE
import kotlin.test.*

class TileSetSystemTest {

    @BeforeTest
    fun init() {
        TestApp
        AssetSystem.clearSystem()
        ViewSystem.clearSystem()
        EntitySystem.clearSystem()
        CompositeSystem.clearSystem()
        AnimationSystem.clearSystem()
    }

    @Test
    fun testOneTileSet() {

        val layerId = Layer.buildAndActivate {
            name = "testLayer"
            view(BASE_VIEW)
        }

        TextureAsset.build {
            name = "TestTextureAsset"
            resourceName = "firefly/fireflyMicroFont.png"
        }

        TileSet.build {
            name = "TestTileSet1"
            texture("TestTextureAsset")
            view(ViewSystem.baseView)
            layer("testLayer")
            blend = BlendMode.ADDITIVE_ALPHA
            tint = WHITE.mutable
            tile {
                sprite {
                    textureBounds(0,0, 8, 8)
                    hFlip = true
                }
                animation {
                    frame {
                        interval = 100
                        protoSprite {
                            name = "s1"
                            textureBounds(8,0, 8, 8)
                        }
                        protoSprite {
                            name = "s2"
                            textureBounds(16,0, 8, 8)
                        }
                    }
                }
            }
            tile {
                sprite {
                    textureBounds(0,0, 8, 8)
                }
                blendMode = BlendMode.ADDITIVE_ALPHA
                contactMask = BitMask()
                contactType = UNDEFINED_CONTACT_TYPE
                material = UNDEFINED_MATERIAL
                tintColor = WHITE.mutable
            }
            tile {
                sprite {
                    textureBounds(0,0, 8, 8)
                }
            }
        }



        // after TileSet creation a SpriteSetAsset with the same name must have been created
        val spriteSetAsset: SpriteSetAsset = FFContext[SpriteSetAsset, "TestTileSet1"]
        assertNotNull(spriteSetAsset)
        // The SpriteSetAsset should not be active/loaded yet
        assertFalse(FFContext.isActive(SpriteSetAsset, "TestTileSet1"))

        // activate the TileSet should also activate the SpriteSetAsset and the TextureAsset
        FFContext.activate(TileSet, "TestTileSet1")
        assertTrue(FFContext.isActive(TileSet, "TestTileSet1"))
        assertTrue(FFContext.isActive(SpriteSetAsset, "TestTileSet1"))
        assertTrue(FFContext.isActive(TextureAsset, "TestTextureAsset"))

        assertNotNull(TileSetContext["TestTileSet1"])
        assertEquals(0, TileSetContext.getTileEntityRef(0, layerId))
        assertEquals(1, TileSetContext.getTileEntityRef(1, layerId))
        assertEquals(2, TileSetContext.getTileEntityRef(2, layerId))
        assertEquals(-1, TileSetContext.getTileEntityRef(3, layerId))

        // creating and activate a second tileset
        TileSet.build {
            name = "TestTileSet2"
            texture("TestTextureAsset")
            view(ViewSystem.baseView)
            layer("testLayer")
            tile {
                sprite {
                    textureBounds(0,0, 8, 8)
                }
            }
            tile {
                sprite {
                    textureBounds(8,0, 8, 8)
                }
            }
            tile {
                sprite {
                    textureBounds(16,0, 8, 8)
                }
            }
        }

        FFContext.activate(TileSet, "TestTileSet2")
        assertTrue(FFContext.isActive(TileSet, "TestTileSet2"))
        assertTrue(FFContext.isActive(SpriteSetAsset, "TestTileSet2"))
        assertTrue(FFContext.isActive(TextureAsset, "TestTextureAsset"))

        assertNotNull(TileSetContext["TestTileSet2"])
        assertEquals(0, TileSetContext.getTileEntityRef(0, layerId))
        assertEquals(1, TileSetContext.getTileEntityRef(1, layerId))
        assertEquals(2, TileSetContext.getTileEntityRef(2, layerId))
        assertEquals(3, TileSetContext.getTileEntityRef(3, layerId))
        assertEquals(4, TileSetContext.getTileEntityRef(4, layerId))
        assertEquals(5, TileSetContext.getTileEntityRef(5, layerId))
        assertEquals(-1, TileSetContext.getTileEntityRef(6, layerId))

        // now deactivating the first tileset should result in a mapping shift
        FFContext.deactivate(TileSet, "TestTileSet1")
        assertFalse(FFContext.isActive(TileSet, "TestTileSet1"))
        assertTrue(FFContext.isActive(TileSet, "TestTileSet2"))
        assertTrue(FFContext.isActive(TextureAsset, "TestTextureAsset"))

        assertEquals(3, TileSetContext.getTileEntityRef(0, 0))
        assertEquals(4, TileSetContext.getTileEntityRef(1, 0))
        assertEquals(5, TileSetContext.getTileEntityRef(2, 0))
    }
}