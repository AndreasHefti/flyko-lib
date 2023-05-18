package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.*
import com.inari.firefly.core.api.*
import com.inari.firefly.game.actor.*
import com.inari.firefly.game.world.*
import com.inari.firefly.game.world.Room.Companion.ROOM_TRANSITION_CONTACT_TYPE
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Scene
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.contact.SimpleContactMap
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.VelocityVerletIntegrator
import com.inari.util.collection.Attributes
import org.lwjgl.glfw.GLFW

fun main() {
    DesktopApp("AreaTest", 800, 600) {

        Engine.GLOBAL_VALUES[Engine.GLOBAL_SHOW_GIZMOS] = "true"
        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        AreaTest.initControl()
        AreaTest.initView()
        AreaTest.createArea()
        AreaTest.runArea()

        ComponentSystem.dumpInfo()
    }
}

object AreaTest {

    const val VIEW_NAME = "testView"
    const val CAMERA_NAME = "Player_Camera"
    const val AREA_NAME = "TestArea"
    const val room1Name = "Room1"
    const val room2Name = "Room2"
    const val PLAYER_NAME = "player1"

    fun initView() {
        View {
            autoActivation = true
            name = VIEW_NAME
            bounds(0, 0, 800, 600)
            zoom = 0.25f
//            withShader {
//                name = "ShaderEffect1"
//                vertexShaderResourceName = "firefly/vertRetro.glsl"
//                fragmentShaderResourceName = "firefly/fragRetro.glsl"
//                shaderUpdate = { adapter ->
//                    adapter.setUniformVec2("u_res", View.baseViewPortProjectionSize)
//                }
//            }
            withControl(SimplePlayerCamera) {
                name = CAMERA_NAME
                pixelPerfect = false
            }
            withLayer {
                name = "background1"
                zPosition = 0
            }
            withLayer {
                name = "main_layer"
                zPosition = 1
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
        Engine.input.defaultDevice = "INPUT_DEVICE_KEY1"
    }

    fun  createArea() {
        initTasksAndScenes()
        JsonAreaLoadTask( attributes = Attributes() + ( ATTR_RESOURCE to "json_example/area.json"))
    }

    fun runArea() {

        Area.load(AREA_NAME)

        createPlayer()

        val room = Room[room1Name]
        room.playerRef(PLAYER_NAME)
        Room.activate(room)
    }


    fun initTasksAndScenes() {

        val fadeId = Entity {
            name = "fadeInOut"
            withComponent(ETransform) {
                viewRef(VIEW_NAME)
                layerRef("main_layer")
                position(0, 0)
            }
            withComponent(EShape) {
                type = ShapeType.RECTANGLE
                color1(0f, 0f, 0f, 1f)
                vertices = floatArrayOf(0f, 0f, 2000f, 2000f)
                blend = BlendMode.NORMAL_ALPHA
                fill = true
            }
        }

        Scene {
            name = "RoomActivationScene"
            val entity = Entity[fadeId]
            initAction = object : Action {
                override fun invoke(index: EntityIndex): ActionResult {
                    val color = entity[EShape].color1
                    color.a = 1f
                    Entity.activate(fadeId)
                    return ActionResult.SUCCESS
                }
            }
            updateAction = object : Action {
                override fun invoke(index: EntityIndex): ActionResult {
                    val color = entity[EShape].color1
                    color.a = color.a - .05f
                    return if (color.a <= 0f) {
                        Entity.deactivate(fadeId)
                        ActionResult.SUCCESS
                    } else ActionResult.RUNNING
                }
            }
        }

        Scene {
            name = "RoomDeactivationScene"
            //var fadeProgress = false
            val entity = Entity[fadeId]
            initAction = object : Action {
                override fun invoke(index: EntityIndex): ActionResult {
                    val color = entity[EShape].color1
                    color.a = 0f
                    Entity.activate(fadeId)
                    return ActionResult.SUCCESS
                }
            }
            updateAction = object : Action {
                override fun invoke(index: EntityIndex): ActionResult {
                    val color = entity[EShape].color1
                    color.a = color.a + .05f
                    return if (color.a >= 1f) {
                        Entity.deactivate(fadeId)
                        ActionResult.SUCCESS
                    } else ActionResult.RUNNING
                }
            }
        }
    }

    fun createPlayer() {
        SimpleContactMap {
            viewRef(VIEW_NAME)
            layerRef("main_layer")
        }

        println("Create Player")
        Player {
            name = PLAYER_NAME
            cameraRef(CAMERA_NAME)
            withPlayerEntity {
                name = PLAYER_NAME
                withComponent(ETransform) {
                    viewRef(VIEW_NAME)
                    layerRef("main_layer")
                    pivot(8, 8)
                    position(2 * 16, 1 * 16)
                }
                withComponent(ESprite) {
                    spriteRef(Sprite {
                        autoActivation = true
                        name = "playerSprite"
                        textureRef("tiled_tileset_example/atlas1616.png")
                        textureBounds(7 * 16, 1 * 16, 16, 16)
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
                    }
                }
                withComponent(EContact) {
                    val fullContactId = withConstraint {
                        fullScan = true
                        bounds(6, 8, 4, 4)
                        typeFilter + ROOM_TRANSITION_CONTACT_TYPE
                    }
                    val terrainContactsId = withConstraint {
                        fullScan = true
                        bounds(4, 1, 8, 19)
                        materialFilter + TileMaterialType.TERRAIN
                    }

                    contactConstraintRef(fullContactId)
                    withContactCallbackConstraint {
                        contactType = ROOM_TRANSITION_CONTACT_TYPE
                        callback = Area.roomTransitionCallback
                    }
                    withResolver(PlatformerCollisionResolver) {
                        terrainContactConstraintRef(terrainContactsId)
                    }
                }
                if (Engine.GLOBAL_VALUES.getBoolean(Engine.GLOBAL_SHOW_GIZMOS))
                    withComponent(EShape) {
                        type = ShapeType.RECTANGLE
                        vertices = floatArrayOf(6f, 8f, 4f, 4f, 4f, 1f, 8f, 19f)
                        fill = false
                        color1(1f, 0f, 0f, 1f)
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
        }
        UpdateControl {
            name = "MoveAspectTracker"
            updateOp = {
                //println(Player[PLAYER_NAME].playerMovement!!.aspects)
            }
        }
    }
}

