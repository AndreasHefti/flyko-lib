package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Task
import com.inari.firefly.game.*
import com.inari.firefly.game.world.*
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.view.View
import com.inari.util.collection.Attributes
import examples.TestCameraController

fun main() {
    DesktopApp("TiledTileMapTest", 800, 600, debug = true) {

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
            withLayer {
                name = "background1"
                zPosition = 0
            }
            withLayer {
                name = "main_layer"
                zPosition = 1
            }
        }

        Task {
            name = "onLoadRoom"
            simpleTask = {
                println("After Room loaded")
            }
        }
        Task {
            name = "onActivationRoom"
            simpleTask = {
                println("After Room activation")
            }
        }
        Task {
            name = "onDeactivateRoom"
            simpleTask = {
                println("Before Room Deactivated")
            }
        }


        val tiledMapAttrs = Attributes() +
                ( ATTR_VIEW_NAME to "testView" ) +
                ( ATTR_TILE_SET_DIR_PATH to "tiled_tileset_example/" ) +
                ( ATTR_RESOURCE to "tiled_map_example/example_map1.json")

        TiledRoomLoadTask(attributes = tiledMapAttrs)
        Room.activate("Room1")
    }
}