package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.Task
import com.inari.firefly.core.UpdateControl
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputAPIImpl
import com.inari.firefly.game.actor.player.PlatformerCollisionResolver
import com.inari.firefly.game.actor.player.PlatformerHMoveController
import com.inari.firefly.game.actor.player.PlatformerJumpController
import com.inari.firefly.game.actor.player.Player
import com.inari.firefly.game.room.Room
import com.inari.firefly.game.room.TileMaterialType
import com.inari.firefly.game.room.tiled_binding.TiledRoomLoadTask
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.SimpleCameraController
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.contact.CollisionResolver
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.Movement
import com.inari.firefly.physics.movement.SimpleStepIntegrator
import com.inari.firefly.physics.movement.VelocityVerletIntegrator
import com.inari.util.collection.Attributes
import examples.TestCameraController
import org.lwjgl.glfw.GLFW

fun main() {
    DesktopApp("PlayerRoomTest", 800, 600) {

        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        initControl()
        initView()
        initTasks()

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

fun initView() {
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
        withControl(SimpleCameraController) {
            name = "Player_Camera"
            snapToBounds(0, 0, 840, 840)
            pixelPerfect = false
        }
    }
}

fun initTasks() {
    Task {
        name = "onLoadRoom"
        simpleTask = ::createPlayer
    }

    Task {
        name = "onActivationRoom"
        simpleTask = {
            println("Activate Player")
            // connect player to camera
            Player.load("player1")
            SimpleCameraController["Player_Camera"].pivot = Player["player1"].playerPosition
            SimpleCameraController["Player_Camera"].adjust()
            Player.activate("player1")

        }
    }

    Task{
        name = "RoomTransitionBuildTask"
        operation = { roomIndex, attributes, callback ->
            println("RoomTransitionBuildTask on Room ${Room[roomIndex]}")
            println(attributes.toString())
        }
    }
}

fun initControl() {
    val keyInput1 = Engine.input
        .createDevice<InputAPIImpl.GLFWDesktopKeyboardInput>(
            "INPUT_DEVICE_KEY1",
            InputAPIImpl.GLFWDesktopKeyboardInput
        )
    keyInput1.mapKeyInput(ButtonType.UP, GLFW.GLFW_KEY_W)
    keyInput1.mapKeyInput(ButtonType.DOWN, GLFW.GLFW_KEY_S)
    keyInput1.mapKeyInput(ButtonType.RIGHT, GLFW.GLFW_KEY_D)
    keyInput1.mapKeyInput(ButtonType.LEFT, GLFW.GLFW_KEY_A)
    keyInput1.mapKeyInput(ButtonType.FIRE_1, GLFW.GLFW_KEY_SPACE)
    keyInput1.mapKeyInput(ButtonType.FIRE_2, GLFW.GLFW_KEY_RIGHT_ALT)
    keyInput1.mapKeyInput(ButtonType.ENTER, GLFW.GLFW_KEY_ENTER)
    keyInput1.mapKeyInput(ButtonType.BUTTON_0, GLFW.GLFW_KEY_P)
    keyInput1.mapKeyInput(ButtonType.QUIT, GLFW.GLFW_KEY_ESCAPE)
}

fun createPlayer() {
    println("Create Player")
    Player {
        name = "player1"
        withPlayerEntity {
            name = "player1"
            withComponent(ETransform) {
                viewRef("testView")
                layerRef("main_layer")
                pivot(8, 8)
                position(2 * 16, 1 * 16)
            }
            withComponent(ESprite) {
                spriteRef(Sprite {
                    autoActivation = true
                    name = "playerSprite"
                    textureRef("tiled_tileset_example/atlas1616.png")
                    textureRegion(7 * 16, 1 * 16, 16, 16)
                })
                blendMode = BlendMode.NORMAL_ALPHA
            }
            withComponent(EMovement) {
                mass = 60f
                maxVelocityWest = 50f
                maxVelocityEast = 50f
                maxVelocityNorth = 200f
                maxVelocitySouth = 200f
                withIntegrator(VelocityVerletIntegrator) {
                    massFactor = 1f
                    //adjustGround = false
                }
            }
            withComponent(EContact) {
                val fullContactId = withConstraint {
                    fullScan = true
                    bounds(3, 1, 9, 14)
                }
                val terrainContactsId = withConstraint {
                    fullScan = true
                    bounds(4, 1, 8, 19)
                    materialFilter + TileMaterialType.TERRAIN
                }
                withResolver(PlatformerCollisionResolver) {
                    fullContactConstraintRef(fullContactId)
                    terrainContactConstraintRef(terrainContactsId)
                }
            }
        }
        withControl(PlatformerHMoveController) {
            inputDevice = Engine.input.getDevice("INPUT_DEVICE_KEY1")
        }
        withControl(PlatformerJumpController) {
            doubleJump = true
            inputDevice = Engine.input.getDevice("INPUT_DEVICE_KEY1")
        }
    }
    UpdateControl {
        name = "MoveAspectTracker"
        updateOp = {
            println(Player["player1"].playerMovement!!.aspects)
        }
    }
}

