package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.EMovement
import kotlin.random.Random

fun main(args: Array<String>) {
    DesktopApp( "CoverCodeTest", 800, 600, debug = true) {
        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        Texture.build {
            name = "nanoTexture"
            resourceName = "firefly/logo.png"
            withSprite {
                name = "nanoSprite"
                textureBounds(0, 0, 32, 32)
                hFlip = false; vFlip = false
            }
        }

        Entity.setMinCapacity(100000)
        val vert = floatArrayOf(0f, 0f, 10f, 20f)
        for (i in 1..10000) {
            Entity {
                autoActivation = true
                withComponent(ETransform) {
                    viewRef(0)
                    position(Random.nextInt(0,800), Random.nextInt(0,600))
                }
                withComponent(ESprite) {
                    spriteRef("nanoSprite")
                }
                withComponent(EMovement) {
                    velocity.x = 0f
                    updateResolution = 100f
                }
            }
        }
    }
}