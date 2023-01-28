package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.api.DesktopAppAdapter
import com.inari.firefly.game.tile.TileUtils
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
            "",
            bitmask.toString())

//        bounds.x = 1
//        bitmask = TileUtils.loadTileContactBitMask(sampleTexture, bounds)
//        assertEquals(
//            "BitMask [region=[x=0,y=0,width=16,height=16], bits=\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111\n" +
//                    "1111111111111111]",
//            bitmask.toString())


        it.dispose()
        DesktopAppAdapter.exit()
    }
}