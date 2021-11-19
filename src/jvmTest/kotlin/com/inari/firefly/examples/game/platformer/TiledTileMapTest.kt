package com.inari.firefly.examples.game.platformer

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.BlendMode
import com.inari.firefly.DesktopApp
import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.control.EControl
import com.inari.firefly.control.task.SimpleTask
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.FFInput
import com.inari.firefly.entity.Entity
import com.inari.firefly.game.world.SimpleCameraController
import com.inari.firefly.game.collision.PlatformerCollisionResolver
import com.inari.firefly.game.player.movement.*
import com.inari.firefly.game.tile.TileMapSystem
import com.inari.firefly.game.tile.TileMaterialType
import com.inari.firefly.game.json.TiledMapJSONAsset
import com.inari.firefly.game.world.Area
import com.inari.firefly.game.world.Room
import com.inari.firefly.game.world.WorldOrientationType
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


class TiledTileMapTest : DesktopApp() {

    override val title: String = "TiledTileMapTest"

    override fun init() {
        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        TileMapSystem
        MovementSystem
        ContactSystem
        TestGameObject

        // control
        val keyInput1 = FFContext.input
            .createDevice<FFInput.GLFWDesktopKeyboardInput>("INPUT_DEVICE_KEY1", FFInput.GLFWDesktopKeyboardInput)
        keyInput1.mapKeyInput(ButtonType.UP, GLFW.GLFW_KEY_W)
        keyInput1.mapKeyInput(ButtonType.DOWN, GLFW.GLFW_KEY_S)
        keyInput1.mapKeyInput(ButtonType.RIGHT, GLFW.GLFW_KEY_D)
        keyInput1.mapKeyInput(ButtonType.LEFT, GLFW.GLFW_KEY_A)
        keyInput1.mapKeyInput(ButtonType.FIRE_1, GLFW.GLFW_KEY_SPACE)
        keyInput1.mapKeyInput(ButtonType.FIRE_2, GLFW.GLFW_KEY_RIGHT_ALT)
        keyInput1.mapKeyInput(ButtonType.ENTER, GLFW.GLFW_KEY_ENTER)
        keyInput1.mapKeyInput(ButtonType.BUTTON_0, GLFW.GLFW_KEY_P)
        keyInput1.mapKeyInput(ButtonType.QUIT, GLFW.GLFW_KEY_ESCAPE)

        val viewId = View.buildAndActivate {
            name = "myView"
            fboScale = 2f
            bounds(0, 0, 640, 640)
            zoom = .5f
            withLayer { name = "Layer3" }
            withLayer { name = "Layer2" }
            withLayer { name = "Layer1" }
        }

        Area.build {
            name = "TiledMapTestArea"

            withRoom {
                name = "Room1"
                areaOrientationType = WorldOrientationType.SECTION
                areaOrientation(0,0, 2, 2)

                withAsset(TiledMapJSONAsset) {
                    name = "mapAsset"
                    resourceName = "tiles/map1.json"
                    view(viewId)
                }

                withLoadTask(SimpleTask) {
                    name = "Room1LoadTask"
                    withOperation { id ->
                        println("loadRoom -> $id")

                        SimpleVelocityStepIntegrator.buildAndActivate {
                            name = "moveIntegrator"
                            //gravityVec(0f,0f)
                        }
                    }
                }

                withActivationTask(SimpleTask) {
                    name = "Room1ActivationTask"
                    withOperation { id ->
                        println("activate -> $id")

                        FFContext.activate(Asset, "mapAsset")
                        TileMapSystem.activateTileMap(FFContext[Asset, "mapAsset"].instanceId)

                        // player
                        val playerEntityId = Entity.buildAndActivate {
                            name = "player"
                            withComponent(EControl) {
                                withController(PlatformerHMoveController) {
                                    updateResolution = 20f
                                    inputDevice = FFContext.input.getDevice("INPUT_DEVICE_KEY1")
                                }
                                withController(PlatformerJumpController) {
                                    inputDevice = FFContext.input.getDevice("INPUT_DEVICE_KEY1")
                                }
                            }
                            withComponent(ETransform) {
                                view(viewId)
                                layer("Layer1")
                                position(4 * 16, 4 * 16)
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
                                    bounds(3,1,9,14)
                                }
                                val terrainContactsId = withConstraint(ContactConstraint) {
                                    bounds(4,1,8,19)
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

                        // camera
                        val camId = SimpleCameraController.buildAndActivate {
                            name="Player_Camera"
                            pivot = FFContext[ETransform, playerEntityId].position
                            snapToBounds(-100, -100, 840, 840)
                        }
                        FFContext[View, viewId].withController(camId)
                    }
                }

                withDeactivationTask(SimpleTask) {
                    name = "Room1DeactivationTask"
                    withOperation { id -> println("deactivate -> $id") }
                }
                withDisposeTask(SimpleTask) {
                    name = "Room1DisposeTask"
                    withOperation { id -> println("dispose -> $id") }
                }

                withPauseTask(SimpleTask) {
                    name = "Room1PauseTask"
                    withOperation { id -> println("pauseRoom -> $id") }
                }
                withResumeTask(SimpleTask) {
                    name = "Room1ResumeTask"
                    withOperation { id -> println("pauseResume -> $id") }
                }

                withActivationScene {
                    name = "Room1ActivationScene"
                    updateResolution = 1f
                    withUpdate {
                        println("Room1ActivationScene Update")
                        OpResult.SUCCESS
                    }
                    withCallback {
                        println("Room1ActivationScene Finished")
                    }
                }

                withDeactivationScene {
                    name = "Room1DeactivationScene"
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
        }


        FFContext.activate(Room, "Room1")

//        FFContext.registerListener<IntConsumer>(ContactEvent) { entityId ->
//            println("contact event: $entityId")
//        }



        //println(FFContext.dump(true))
        //println(Indexer.dump())
    }
}

fun main(args: Array<String>) {
    try {
        val config = Lwjgl3ApplicationConfiguration()
        config.setResizable(true)
        config.setWindowedMode(640, 640)
        Lwjgl3Application(TiledTileMapTest(), config)
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}