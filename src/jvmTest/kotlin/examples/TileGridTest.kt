package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.EMultiplier
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGrid
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View

fun main() {
    DesktopApp("TileGridTest", 800, 600) {
        Texture.build {
            name = "logoTexture"
            resourceName = "firefly/logo.png"
            withSprite {
                name = "inariSprite1"
                textureBounds(0, 0, 32, 32)
                hFlip = false; vFlip = false
            }
        }

        val viewKey = View {
            autoActivation = true
            name = "view1"
            bounds(0, 0, 800, 600)
            blendMode = BlendMode.NORMAL_ALPHA
            //zoom = 0.5f
            withLayer { name = "layer1"; position(0, 50) }
        }

        TileGrid {
            autoActivation = true
            name = "tileMap1"
            viewRef(viewKey)
            layerRef("layer1")
            position(0, -50)
            cellDim(32, 32)
            gridDim(5, 5)
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) { viewRef(viewKey); layerRef("layer1") }
            withComponent(ETile) {
                spriteRef("inariSprite1")
                tileGridRef("tileMap1")
            }
            withComponent(EMultiplier) {
                for (y in 0 until 5) {
                    for (x in 0 until 5) {
                        if ((y % 2 > 0 && x % 2 > 0) || (y % 2 == 0 && x % 2 == 0) ) {
                            positions.add(x.toFloat())
                            positions.add(y.toFloat())
                        }
                    }
                }
            }
        }

        TileGrid {
            autoActivation = true
            name = "tileMap2"
            viewRef(viewKey)
            layerRef("layer1")
            position(100, 150)
            cellDim(32, 32)
            gridDim(5, 5)
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) { viewRef(viewKey); layerRef("layer1") }
            withComponent(ETile) {
                spriteRef("inariSprite1")
                tileGridRef("tileMap2")
            }
            withComponent(EMultiplier) {
                for (y in 0 until 5) {
                    for (x in 0 until 5) {
                        if ((y % 2 > 0 && x % 2 > 0) || (y % 2 == 0 && x % 2 == 0) ) {
                            positions.add(x.toFloat())
                            positions.add(y.toFloat())
                        }
                    }
                }
            }
        }
    }
}