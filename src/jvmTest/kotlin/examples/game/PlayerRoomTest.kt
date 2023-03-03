package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.*
import com.inari.firefly.core.api.*
import com.inari.firefly.game.actor.player.PlatformerCollisionResolver
import com.inari.firefly.game.actor.player.PlatformerHMoveController
import com.inari.firefly.game.actor.player.PlatformerJumpController
import com.inari.firefly.game.actor.player.Player
import com.inari.firefly.game.room.Room
import com.inari.firefly.game.room.TileMaterialType
import com.inari.firefly.game.room.tiled_binding.TiledRoomLoadTask
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Scene
import com.inari.firefly.graphics.view.SimpleCameraController
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.contact.SimpleContactMap
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.VelocityVerletIntegrator
import com.inari.util.collection.Attributes
import com.inari.util.collection.Dictionary
import com.inari.util.geom.Vector4f
import com.inari.util.sleepCurrentThread
import org.lwjgl.glfw.GLFW

const val showGizmos = true
const val tileSetName = "TiledMapTest"
fun main() {
    DesktopApp("PlayerRoomTest", 800, 600) {

        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        initControl()
        initView()
        initTasksAndScenes()

        val tiledMapAttrs = Attributes() +
                ( TiledRoomLoadTask.ATTR_NAME to tileSetName ) +
                ( TiledRoomLoadTask.ATTR_VIEW_NAME to "testView" ) +
                ( TiledRoomLoadTask.ATTR_TILE_SET_DIR_PATH to "tiled_tileset_example/" ) +
                ( TiledRoomLoadTask.ATTR_RESOURCE to "tiled_map_example/example_map1.json")

        Room.loadInParallel = true
        Room.stopLoadSceneWhenLoadFinished = false
        Room.disposeAfterDeactivation = false
        TiledRoomLoadTask(attributes = tiledMapAttrs)
        val room = Room[tileSetName]
        room.loadScene("RoomLoadScene")
        Room.activate(room)
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
            snapToBounds(0, 0, 320, 160)
            pixelPerfect = false
        }
        withLayer { name = "background1" }
        withLayer { name = "main_layer" }
    }

    SimpleContactMap {
        viewRef("testView")
        layerRef("main_layer")
    }
}

fun initTasksAndScenes() {
    Task {
        name = "onLoadRoom"
        operation = { key, attrs, _ -> createPlayer(key, attrs) }
    }

    Task {
        name = "onActivationRoom"
        simpleTask = {
            println("Activate Player")
            val room = Room[tileSetName]
            room.inputDevice = Engine.input.getDevice("INPUT_DEVICE_KEY1")
            room.withPlayer("player1")
        }
    }

    Task{
        name = "RoomTransitionBuildTask"
        operation = { roomKey, attributes, _ ->
            println("RoomTransitionBuildTask on Room ${Room[roomKey]} $attributes")
            val bounds = Vector4f()
            bounds(attributes["bounds"]!!)
            Room[roomKey].withLifecycleComponent(
                Entity {
                    autoActivation = true
                    withComponent(ETransform) {
                        viewRef("testView")
                        layerRef("main_layer")
                        position(bounds)
                    }
                    if (showGizmos)
                        withComponent(EShape) {
                            type = ShapeType.RECTANGLE
                            vertices = floatArrayOf(0f,0f,bounds.width, bounds.height)
                            fill = false
                            color(1f, 0f, 0f, 1f)
                            blend = BlendMode.NORMAL_ALPHA
                        }
                    withComponent(EContact) {
                        contactBounds(0, 0, bounds.width.toInt(), bounds.height.toInt())
                        contactType = EContact.CONTACT_TYPE_ASPECT_GROUP.createAspect("ROOM_TRANSITION_1")
                    }
                    withComponent(EAttribute) { this.attributes = attributes }
            })
        }
    }

    Scene {
        var tick = 0
        name = "RoomLoadScene"
        updateOperation =  {
            tick++
            if (tick % 10 == 0)
                println("RoomLoadScene $tick")
            if (tick > 100)
                OperationResult.SUCCESS
            else
                OperationResult.RUNNING
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
    keyInput1.mapKeyInput(ButtonType.PAUSE, GLFW.GLFW_KEY_P)
    keyInput1.mapKeyInput(ButtonType.QUIT, GLFW.GLFW_KEY_ESCAPE)
}

fun createPlayer(roomKey: ComponentKey, attributes: Dictionary) {
    for (i in 0..5) {
        println("RoomLoadDelay $i")
        sleepCurrentThread(200)
    }

    println("Create Player")
    val playerKey = Player {
        name = "player1"
        withPlayerEntity {
            name = "player1"
            groups + attributes["name"]!!
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
            val transType = EContact.CONTACT_TYPE_ASPECT_GROUP.createAspect("ROOM_TRANSITION_1")
            withComponent(EContact) {
                val fullContactId = withConstraint {
                    fullScan = true
                    bounds(6, 8, 4, 4)
                    typeFilter + transType
                }
                val terrainContactsId = withConstraint {
                    fullScan = true
                    bounds(4, 1, 8, 19)
                    materialFilter + TileMaterialType.TERRAIN
                }

                withResolver(PlatformerCollisionResolver) {
                    fullContactConstraintRef(fullContactId)
                    terrainContactConstraintRef(terrainContactsId)
                    withFullContactCallback(contact = transType) {
                        if (it.hasContactOfType(transType)) {
                            if (it.contactMask.cardinality > 8) {
                                println(it.contactMask)
                                val contactWith = Entity[it.getFirstContactOfType(transType).entityId]
                                handleRoomTransition(contactWith[EAttribute].attributes)
                            }
                            true
                        } else false
                    }
                }
            }
            if (showGizmos)
                withComponent(EShape) {
                    type = ShapeType.RECTANGLE
                    vertices = floatArrayOf(6f, 8f, 4f, 4f, 4f, 1f, 8f, 19f)
                    fill = false
                    color(1f, 0f, 0f, 1f)
                    blend = BlendMode.NORMAL_ALPHA
                }
        }
        withControl(PlatformerHMoveController) {
            name = "PlatformerHMoveController"
            inputDevice = Engine.input.getDevice("INPUT_DEVICE_KEY1")
        }
        withControl(PlatformerJumpController) {
            name = "PlatformerJumpController"
            doubleJump = true
            inputDevice = Engine.input.getDevice("INPUT_DEVICE_KEY1")
        }
        withCamera(SimpleCameraController["Player_Camera"])
    }
    UpdateControl {
        name = "MoveAspectTracker"
        updateOp = {
            //println(Player["player1"].playerMovement!!.aspects)
        }
    }
    Room[roomKey].withLifecycleComponent(playerKey)

}

fun handleRoomTransition(attributes: Dictionary) {
    println("Room Transition: $attributes")
}

