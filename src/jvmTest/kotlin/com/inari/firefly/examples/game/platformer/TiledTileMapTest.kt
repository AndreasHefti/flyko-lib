package com.inari.firefly.examples.game.platformer

import com.inari.firefly.BlendMode
import com.inari.firefly.DesktopRunner
import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.control.EControl
import com.inari.firefly.control.scene.Scene
import com.inari.firefly.control.task.SimpleTask
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.FFInput
import com.inari.firefly.entity.Entity
import com.inari.firefly.game.collision.PlatformerCollisionResolver
import com.inari.firefly.game.player.movement.*
import com.inari.firefly.game.tile.TileMapSystem
import com.inari.firefly.game.tile.TileMaterialType
import com.inari.firefly.game.json.TiledMapJSONAsset
import com.inari.firefly.game.player.PlayerComposite
import com.inari.firefly.game.player.PlayerSystem
import com.inari.firefly.game.world.*
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.graphics.view.View
import com.inari.firefly.info.FFInfoSystem
import com.inari.firefly.info.FrameRateInfo
import com.inari.firefly.physics.contact.ContactConstraint
import com.inari.firefly.physics.contact.ContactSystem
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.MovementSystem
import com.inari.util.OpResult
import org.lwjgl.glfw.GLFW


fun initControl() {
    val keyInput1 = FFContext.input
        .createDevice<FFInput.GLFWDesktopKeyboardInput>(
            "INPUT_DEVICE_KEY1",
            FFInput.GLFWDesktopKeyboardInput
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

    SimpleVelocityStepIntegrator.buildAndActivate {
        name = "moveIntegrator"
        //gravityVec(0f,0f)
    }
}

fun initView() {
    View.buildAndActivate {
        name = "testView"
        fboScale = 2f
        bounds(0, 0, 640, 640)
        zoom = .5f
        withLayer { name = "Layer3" }
        withLayer { name = "Layer2" }
        withLayer { name = "Layer1" }
    }
}

fun initPlayerTasks() {
    SimpleTask.build {
        name = "LoadPlayer"
        withOperation { id ->
            println("load Player $id")

            Entity.build {
                name = PlayerComposite.PLAYER_ENTITY_NAME
                withComponent(EControl) {
                    withController(PlatformerHMoveController) {
                        updateResolution = 20f
                        inputDevice = FFContext.input.getDevice("INPUT_DEVICE_KEY1")
                    }
                    withController(PlatformerJumpController) {
                        doubleJump = true
                        inputDevice = FFContext.input.getDevice("INPUT_DEVICE_KEY1")
                    }
                }
                withComponent(ETransform) {
                    view("testView")
                    layer("Layer1")
                    pivot(8, 8)
                    //position(4 * 16, 4 * 16)
                }
                withComponent(ESprite) {
                    sprite(SpriteAsset.buildAndActivate {
                        name = "playerSprite"
                        texture("tileSetAtlas_bluTerrain_tileSetAsset")
                        textureRegion(7 * 16, 1 * 16, 16, 16)
                    })
                    blend = BlendMode.NORMAL_ALPHA
                }
                withComponent(EMovement) {
                    integrator("moveIntegrator")
                    mass = 25f
                    maxVelocityWest = 50f
                    maxVelocityEast = 50f
                    maxVelocityNorth = 200f
                    maxVelocitySouth = 100f
                    //force(0.0f, -8.8f)
                }
                withComponent(EContact) {
                    val fullContactId = withConstraint(ContactConstraint) {
                        bounds(3, 1, 9, 14)
                    }
                    val terrainContactsId = withConstraint(ContactConstraint) {
                        bounds(4, 1, 8, 19)
                        materialFilter + TileMaterialType.TERRAIN_SOLID
                    }
                    withResolver(PlatformerCollisionResolver) {
                        withFullContactConstraint(fullContactId)
                        withTerrainContactConstraint(terrainContactsId)
                        looseGroundContactCallback = {
                            println("loose ground")
                        }
                    }
                }
            }
        }
    }
}

fun initRoomTasks() {
    SimpleTask.build {
        name = "RoomLoadTask"
        withOperation { id ->
            println("loadRoom -> $id")
            PlayerSystem.withPlayerLoadTask("LoadPlayer")
        }
    }

    SimpleTask.build {
        name = "RoomActivationTask"
        withOperation { id ->
            println("activate -> $id")

            FFContext.activate(Asset, "mapAsset")
            TileMapSystem.activateTileMap(FFContext[Asset, "mapAsset"].instanceId)

            // player
            PlayerSystem.loadPlayer()
            // camera
            val camId = SimpleCameraController.buildAndActivate {
                name = "Player_Camera"
                pivot = PlayerSystem.playerPosition
                snapToBounds(-100, -100, 840, 840)
            }
            FFContext[View, "testView"].withController(camId)
        }
    }

    SimpleTask.build {
        name = "RoomDeactivationTask"
        withOperation { id -> println("deactivate -> $id") }
    }
    SimpleTask.build {
        name = "RoomDisposeTask"
        withOperation { id -> println("dispose -> $id") }
    }

    SimpleTask.build {
        name = "RoomPauseTask"
        withOperation { id -> println("pauseRoom -> $id") }
    }
    SimpleTask.build {
        name = "RoomResumeTask"
        withOperation { id -> println("pauseResume -> $id") }
    }
}

fun initScenes() {
    Scene.build {
        name = "RoomActivationScene"
        updateResolution = 1f
        withUpdate {
            println("Room1ActivationScene Update")
            OpResult.SUCCESS
        }
        withCallback {
            println("Room1ActivationScene Finished")
        }
    }

    Scene.build {
        name = "RoomDeactivationScene"
        updateResolution = 1f
        withUpdate {
            println("Room1DeactivationScene Update")
            OpResult.SUCCESS
        }
        withCallback {
            println("Room1DeactivationScene Finished")
        }
    }
}

object TiledTileMapTest {
    @JvmStatic fun main(args: Array<String>) {
        object : DesktopRunner("TiledTileMapTest", 640, 640) {
            override fun init() {
                FFInfoSystem
                    .addInfo(FrameRateInfo)
                    .activate()

                // engine init
                TileMapSystem
                MovementSystem
                ContactSystem
                TestGameObject

                // game init
                initControl()
                initView()

                // area init
                initPlayerTasks()
                initRoomTasks()
                initScenes()

                Area.build {
                    name = "TiledMapTestArea"

                    withRoom {
                        name = "Room1"
                        areaOrientation(0, 0, 2, 2)
                        roomOrientation(0, 0, 20 * 16, 20 * 16)
                        withAsset(TiledMapJSONAsset) {
                            name = "mapAsset"
                            resourceName = "tiles/map1.json"
                            view("testView")
                        }

                        withLoadTask("RoomLoadTask")
                        withActivationTask("RoomActivationTask")
                        withDeactivationTask("RoomDeactivationTask")
                        withDisposeTask("RoomDisposeTask")
                        withActivationScene("RoomActivationScene")
                        withDeactivationScene("RoomDeactivationScene")
                    }
                }

                WorldSystem.startRoom(4 * 16, 4 * 16)("Room1")
            }
        }
    }
}
