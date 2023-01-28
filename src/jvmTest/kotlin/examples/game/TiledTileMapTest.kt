package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Task
import com.inari.firefly.game.tile.tiled_binding.TiledTileMap
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.view.View
import examples.TestCameraController

fun main() {
    DesktopApp("TiledTileMapTest", 800, 600) {

        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        View {
            autoActivation = true
            name = "testView"
            bounds(0, 0, 800, 600)
            zoom = 0.25f
            withShader {
                name = "ShaderEffect1"
                vertexShaderResourceName = "firefly/vertRetro.glsl"
                fragmentShaderResourceName = "firefly/fragRetro.glsl"
                shaderUpdate = { adapter ->
                    adapter.setUniformVec2("u_res", View.baseViewPortProjectionSize)
                }
            }
            withControl(TestCameraController) {}
        }

        Task {
            name = "afterLoadTileMap"
            withSimpleOperation {
                println("Tile Map after load")
            }
        }
        Task {
            name = "beforeActivateTileMap"
            withSimpleOperation {
                println("Tile Map on activation")
            }
        }
        Task {
            name = "afterActivateTileMap"
            withSimpleOperation {
                println("Tile Map after activation")
            }
        }

        TiledTileMap {
            name = "testmap1"
            viewRef("testView")
            tilesetAssetDirectory = "tiled_tileset_example/"
            resourceName = "tiled_map_example/example_map1.json"
        }

        TiledTileMap.activate("testmap1")
    }
}