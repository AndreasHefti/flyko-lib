package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Task
import com.inari.firefly.game.room.Room
import com.inari.firefly.game.room.tiled_binding.TiledRoomLoadTask
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.view.View
import com.inari.util.collection.Attributes
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
            simpleTask = {
                println("After Room loaded")
            }
        }
        Task {
            name = "beforeActivateTileMap"
            simpleTask = {
                println("Before Room activation")
            }
        }
        Task {
            name = "afterActivateTileMap"
            simpleTask = {
                println("After Room activation")
            }
        }

        val tileSetName = "TiledMapTest"
        val tiledMapAttrs = Attributes() +
                ( TiledRoomLoadTask.ATTR_NAME to tileSetName ) +
                ( TiledRoomLoadTask.ATTR_VIEW_NAME to "testView" ) +
                ( TiledRoomLoadTask.ATTR_TILE_SET_DIR_PATH to "tiled_tileset_example/" ) +
                ( TiledRoomLoadTask.ATTR_RESOURCE to "tiled_map_example/example_map1.json")

        TiledRoomLoadTask(attributes = tiledMapAttrs)
        Room.activate(tileSetName)
    }
}