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
import com.inari.firefly.game.json.FireflyJsonAreaAsset
import com.inari.firefly.game.player.movement.*
import com.inari.firefly.game.tile.TileMapSystem
import com.inari.firefly.game.tile.TileMaterialType
import com.inari.firefly.game.json.TiledMapJSONAsset
import com.inari.firefly.game.json.TiledTileMap
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
    // camera
    val camId = SimpleCameraController.buildAndActivate {
        name = "Player_Camera"
        //pivot = PlayerSystem.playerPosition
        snapToBounds(-100, -100, 840, 840)
        pixelPerfect = false
    }
    FFContext[View, "testView"].withController(camId)
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

fun initAreaTasks() {
    SimpleTask.build {
        name = "AreaLoadTask"
        withOperation { id ->
            println("loadArea -> $id")
        }
    }

    SimpleTask.build {
        name = "AreaActivationTask"
        withOperation { id ->
            println("activateArea -> $id")

            val area = FFContext[Area, id]
            WorldSystem.forEachRoom { room ->
                if (room.parentName == area.name) {
                    // load room tiled resource and create tile set asset for the room
                    val tiledMapFile = room.getAttribute("tiledRoomResource")!!
                    val res = FFContext.resourceService.loadJSONResource(tiledMapFile, TiledTileMap::class)
                    TiledMapJSONAsset.build {
                        name = "${room.name}_MapAsset"
                        view("testView")
                        withTiledTileMap(res)
                    }
                }
            }
        }
    }

    SimpleTask.build {
        name = "AreaDeactivationTask"
        withOperation { id ->
            println("deactivateArea -> $id")
        }
    }

    SimpleTask.build {
        name = "AreaDisposeTask"
        withOperation { id ->
            println("disposeArea -> $id")
        }
    }
}

fun initRoomTasks() {
    SimpleTask.build {
        name = "RoomLoadTask"
        withOperation { id ->
            println("loadRoom -> $id")
        }
    }

    SimpleTask.build {
        name = "RoomActivationTask"
        withOperation { id ->
            println("activate -> $id")

            val roomName = FFContext[Room, id].name

            FFContext.load(Asset, "${roomName}_MapAsset")
            TileMapSystem.activateTileMap(FFContext[Asset, "${roomName}_MapAsset"].instanceId)
            PlayerSystem.loadPlayer()

            // connect player to camera
            FFContext[SimpleCameraController, "Player_Camera"].pivot = PlayerSystem.playerPosition
        }
    }

    SimpleTask.build {
        name = "RoomDeactivationTask"
        withOperation { id ->
            println("deactivate -> $id")

            val roomName = FFContext[Room, id].name
            TileMapSystem.deactivateTileMap(FFContext[Asset, "${roomName}_MapAsset"].instanceId)
            FFContext.dispose(Asset, "${roomName}_MapAsset")
        }
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
            println("RoomActivationScene Update")
            OpResult.SUCCESS
        }
        withCallback {
            println("RoomActivationScene Finished")
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
                WorldSystem

                // game init
                initControl()
                initView()
                initScenes()
                // player
                initPlayerTasks()
                PlayerSystem.withPlayerLoadTask("LoadPlayer")

                // area init
                initAreaTasks()
                initRoomTasks()
                val areaId = FireflyJsonAreaAsset.build {
                    name = "TiledMapTestAreaAsset"
                    resourceName = "tiles/testArea.json"
                }

                // load
                FFContext.load(areaId)
                // activate area
                FFContext.activate(Area, "TiledMapTestArea")
                // start game in Room1
                WorldSystem.startRoom(4 * 16, 4 * 16)("Room1")
            }
        }
    }
}
