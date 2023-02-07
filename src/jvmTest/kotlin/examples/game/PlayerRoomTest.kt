package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Task
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.view.View
import examples.TestCameraController

fun main() {
    DesktopApp("PlayerRoomTest", 800, 600) {

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
            name = "beforeTileMapLoad"
            simpleTask = {
                println("Load Player")
            }
        }

        Task {
            name = "beforeTileMapActivation"
            simpleTask = {
                println("Tile Map on activation")
            }
        }

    }
}