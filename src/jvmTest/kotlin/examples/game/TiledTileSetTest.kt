package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.game.world.ATTR_RESOURCE
import com.inari.firefly.game.world.ATTR_TILE_SET_NAME
import com.inari.firefly.game.world.TileSet
import com.inari.firefly.game.world.TiledTileSetLoadTask
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.text.EText
import com.inari.firefly.graphics.text.Font
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.SpriteFrameAnimation
import com.inari.util.collection.Attributes

fun main() {
    DesktopApp("TiledTileSetTest", 800, 600, debug = true) {

        // Load TileSet from Tiled JSON file with TiledTileSetLoadTask
        val tileSetName = "TiledTileSetTest"
        val tiledTileSetAttrs = Attributes() +
            ( ATTR_TILE_SET_NAME to tileSetName ) +
            ( ATTR_RESOURCE to "tiled_tileset_example/tiled_tileset.json")

        TiledTileSetLoadTask(attributes = tiledTileSetAttrs)
        TileSet.activate(tileSetName)

        // Create View and draw each Tile with its collision mask to screen
        View {
            autoActivation = true
            name = "testView"
            bounds(0, 0, 800, 600)
            zoom = 1f
        }

        val offset = 50
        var x = 0
        var y = 0
        TileSet[tileSetName].tiles.forEach {
            Entity {
                autoActivation = true
                withComponent(ETransform) {
                    viewRef("testView")
                    position( offset + x * 64,offset + y * 64)
                }
                withComponent(ESprite) {
                    spriteIndex = it.spriteTemplate.spriteIndex
                    blendMode = it.blendMode ?: BlendMode.NONE
                    tintColor(it.tintColor ?: tintColor)
                }

                it.contactMask?.apply {
                    val list = mutableListOf<Float>()
                    for (y in 0 until 16) {
                        for (x in 0 until 16) {
                            if (this.getBit(x, y)) {
                                list.add(16f + x)
                                list.add(16f + y)
                            }
                        }
                    }
                    this@Entity.withComponent(EShape) {
                        type = ShapeType.POINT
                        color1(1f, 0f, 0f , 1f)
                        vertices = list.toFloatArray()
                    }
                }

                it.animationData?.apply {
                    this@Entity.withComponent(EAnimation) {
                        withAnimation(SpriteFrameAnimation) {
                            animatedProperty = ESprite.PropertyAccessor.SPRITE_INDEX
                            looping = true
                            timeline = this@apply.frames.toArray()
                        }
                    }
                }

            }
            x++
            if (x >= 10) {
                x = 0
                y++
            }
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) {
                viewRef("testView")
                position( 50,300)
            }
            withComponent(EText) {
                fontRef(Font[Engine.SYSTEM_FONT])
                text.append("Blue: Tile-Sprite\nRed: Collision-Map\nLeft-Bottom-Corner: Tile-Animation")
            }
        }

    }
}