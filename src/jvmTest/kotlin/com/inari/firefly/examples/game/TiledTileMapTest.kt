package com.inari.firefly.examples.game

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.BlendMode
import com.inari.firefly.DesktopApp
import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.control.ControllerComposite
import com.inari.firefly.control.EControl
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.FFInput
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.game.camera.CameraPivot
import com.inari.firefly.game.camera.SimpleCameraController
import com.inari.firefly.game.collision.PlatformerCollisionResolver
import com.inari.firefly.game.movement.*
import com.inari.firefly.game.tile.TileMapSystem
import com.inari.firefly.game.tile.TileMaterialType
import com.inari.firefly.game.tiled.TiledMapAsset
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.rendering.RenderingSystem
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.firefly.info.FFInfoSystem
import com.inari.firefly.info.FrameRateInfo
import com.inari.firefly.physics.animation.AnimationSystem
import com.inari.firefly.physics.contact.ContactConstraint
import com.inari.firefly.physics.contact.ContactSystem
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.MovementSystem
import com.inari.util.geom.PositionF
import org.lwjgl.glfw.GLFW


class TiledTileMapTest : DesktopApp() {

    override val title: String = "TiledTileMapTest"

    override fun init() {
        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        ViewSystem
        RenderingSystem
        AssetSystem
        EntitySystem
        ViewSystem
        TileMapSystem
        AnimationSystem
        MovementSystem
        ContactSystem
        TestGameObject

        val playerTextureAsset = TextureAsset.buildAndActivate {
            name = "playerTex"
            resourceName = "tiles/outline_blu_16_16.png"
        }

        val viewId = View.buildAndActivate {
            name = "myView"
            fboScale = 2f
            bounds(0, 0, 640, 640)
            zoom = .5f
        }

        val layerId3 = Layer.buildAndActivate {
            name = "Layer3"
            view(viewId)
        }
        val layerId2 = Layer.buildAndActivate {
            name = "Layer2"
            view(viewId)
        }
        val layerId1 = Layer.buildAndActivate {
            name = "Layer1"
            view(viewId)
        }


        val mapAssetId = TiledMapAsset.build {
            name = "TiledMapExampleAsset"
            resourceName = "tiles/map1.json"
            view(viewId)
        }

        FFContext.activate(mapAssetId)
        val tileMapAsset = FFContext[Asset, mapAssetId]
        TileMapSystem.activateTileMap(tileMapAsset.instanceId)

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

        // movement
        val movIntId = VelocityVerletIntegrator.buildAndActivate {}
        val eulerId = SemiImplicitEulerIntegrator.buildAndActivate {}
        val stepIntId = SimpleVelocityStepIntegrator.buildAndActivate {
            //gravityVec(0f,0f)
        }

        // player
        val playerSpriteId = SpriteAsset.buildAndActivate {
            name = "playerSprite"
            texture(playerTextureAsset)
            textureRegion(7 * 16, 1 * 16, 16, 16)
        }

        val playerEntityId = Entity.buildAndActivate {
            name = "player"
            withComponent(EControl) {
                withActiveController(ControllerComposite) {
                    withActiveController(PlatformerHMoveController) {
                        updateResolution = 20f
                        inputDevice = FFContext.input.getDevice("INPUT_DEVICE_KEY1")
                    }
                    withActiveController(PlatformerJumpController) {
                        inputDevice = FFContext.input.getDevice("INPUT_DEVICE_KEY1")
                    }
                }
            }
            withComponent(ETransform) {
                view(viewId)
                layer(layerId1)
                position(4 * 16, 4 * 16)
            }
            withComponent(ESprite) {
                sprite(playerSpriteId)
                blend = BlendMode.NORMAL_ALPHA
            }
            withComponent(EMovement) {
                integrator(stepIntId)
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
                }
            }
        }

        // camera
        val _pivot: CameraPivot =object : CameraPivot {
            override fun init() {}
            override operator fun invoke(): PositionF = FFContext[ETransform, playerEntityId].position

        }
        val camId = SimpleCameraController.buildAndActivate {
            name="Player_Camera"
            pivot=_pivot
            snapToBounds(-100, -100, 840, 840)
        }
        FFContext.get<View>(viewId).withController(camId)
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