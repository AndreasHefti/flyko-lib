package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.api.DesktopAppAdapter
import com.inari.firefly.game.world.TileUtils
import com.inari.firefly.graphics.sprite.AccessibleTexture
import com.inari.util.geom.Vector4i
import kotlin.test.assertEquals

fun main() {
    DesktopApp("BitmaskTextureSamplingTest", 704, 480) {

        val sampleTexture = AccessibleTexture {
            name = "testTexture"
            resourceName = "tiled_tileset_example/tileset1616.png"
            autoActivation = true
        }

        val bounds = Vector4i(1,2,16,16)

        var bitmask = TileUtils.loadTileContactBitMask(sampleTexture, bounds)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
                    "1111111111111111\n" +
                    "0111111111111111\n" +
                    "0011111111111111\n" +
                    "0001111111111111\n" +
                    "0000111111111111\n" +
                    "0000011111111111\n" +
                    "0000001111111111\n" +
                    "0000000111111111\n" +
                    "0000000011111111\n" +
                    "0000000001111111\n" +
                    "0000000000111111\n" +
                    "0000000000011111\n" +
                    "0000000000001111\n" +
                    "0000000000000111\n" +
                    "0000000000000011\n" +
                    "0000000000000001]",
            bitmask.toString())

        it.dispose()
        DesktopAppAdapter.exit()
    }
}