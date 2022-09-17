package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputAPIImpl
import com.inari.firefly.game.PlatformerCollisionResolver
import com.inari.firefly.game.json.AreaJsonAsset
import com.inari.firefly.game.tile.TileMaterialType
import com.inari.firefly.game.json.TiledTileMap
import com.inari.firefly.game.json.TiledTileMapAsset
import com.inari.firefly.game.tile.TileContactFormType
import com.inari.firefly.game.tile.TileMap
import com.inari.firefly.game.world.*
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Scene
import com.inari.firefly.graphics.view.SimpleCameraController
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.Movement
import com.inari.firefly.physics.movement.VelocityVerletIntegrator
import com.inari.util.OperationResult
import org.lwjgl.glfw.GLFW


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

//    VelocityVerletIntegrator.buildAndActivate {
//        name = "moveIntegrator"
//        massFactor = 1.5f
//    }
//    SimpleStepIntegrator {
//        name = "moveIntegrator"
//        massFactor = 3f
//    }
}

fun initView() {
    View {
        autoActivation = true
        name = "testView"
        fboScale = 2f
        bounds(0, 0, 640, 640)
        zoom = .5f
        withLayer { name = "Layer3" }
        withLayer { name = "Layer2" }
        withLayer { name = "Layer1" }
        withControl(SimpleCameraController) {
            name = "Player_Camera"
            //pivot = PlayerSystem.playerPosition
            snapToBounds(-100, -100, 840, 840)
            pixelPerfect = false
        }
    }
}

fun initPlayerTasks() {
    Task {
        name = "LoadPlayer"
        withSimpleOperation { id ->
            println("create Player Entity $id")
            Entity.build {
                name = "player1"
                withComponent(ETransform) {
                    viewRef("testView")
                    layerRef("Layer1")
                    pivot(8, 8)
                    //position(4 * 16, 4 * 16)
                }
                withComponent(ESprite) {
                    spriteRef(Sprite {
                        autoActivation = true
                        name = "playerSprite"
                        textureRef("tileSetAtlas_bluTerrain_tileSetAsset")
                        textureRegion(7 * 16, 1 * 16, 16, 16)
                    })
                    blendMode = BlendMode.NORMAL_ALPHA
                }
                withComponent(EMovement) {
                    mass = 50f
                    maxVelocityWest = 50f
                    maxVelocityEast = 50f
                    maxVelocityNorth = 200f
                    maxVelocitySouth = 100f
                    withIntegrator(VelocityVerletIntegrator) {
                        massFactor = 1.5f
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
                        materialFilter + TileMaterialType.TERRAIN_SOLID
                    }
                    withResolver(PlatformerCollisionResolver) {
                        fullContactConstraintRef(fullContactId)
                        terrainContactConstraintRef(terrainContactsId)
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
    Task {
        name = "AreaLoadTask"
        withSimpleOperation { id ->
            println("loadArea -> $id")
        }
    }

    Task {
        name = "AreaActivationTask"
        withSimpleOperation { id ->
            println("activateArea -> $id")

            val area = Area[id]
            Room.forEachDo { room ->
                if (room.parent.name == area.name) {
                    // load room tiled resource and create tile set asset for the room
                    TiledTileMapAsset {
                        name = "${room.name}_MapAsset"
                        viewRef("testView")
                        resourceSupplier = {
                            val tiledMapFile = room.getAttribute("tiledRoomResource")!!
                            Engine.resourceService.loadJSONResource(tiledMapFile, TiledTileMap::class) }
                    }
                }
            }
        }
    }

    Task {
        name = "AreaDeactivationTask"
        withSimpleOperation { id ->
            println("deactivateArea -> $id")
        }
    }

    Task {
        name = "AreaDisposeTask"
        withSimpleOperation { id ->
            println("disposeArea -> $id")
        }
    }
}

fun initRoomTasks() {
    Task {
        name = "RoomLoadTask"
        withSimpleOperation { id ->
            println("loadRoom -> ${Room[id]}")
        }
    }

    Task {
        name = "RoomActivationTask"
        withSimpleOperation { id ->
            println("activateRoom -> ${Room[id]}")

            val roomName = Room[id].name

            Asset.load("${roomName}_MapAsset")
            TileMap.activate(Asset["${roomName}_MapAsset"].assetIndex)
            Player.load("player1")

            // connect player to camera
            SimpleCameraController["Player_Camera"].pivot = Player["player1"].playerPosition
        }
    }

    Task {
        name = "RoomDeactivationTask"
        withSimpleOperation { id ->
            println("deactivateRoom -> $id")

            val roomName = Room[id].name
            TileMap.deactivate(Asset["${roomName}_MapAsset"].assetIndex)
            Asset.dispose("${roomName}_MapAsset")
        }
    }
    Task {
        name = "RoomDisposeTask"
        withSimpleOperation { id -> println("disposeRoom -> $id") }
    }

    Task {
        name = "RoomPauseTask"
        withSimpleOperation { id -> println("pauseRoom -> $id") }
    }
    Task {
        name = "RoomResumeTask"
        withSimpleOperation { id -> println("pauseResume -> $id") }
    }
}

fun initScenes() {
    Scene {
        name = "RoomActivationScene"
        updateResolution = 1f
        withUpdate {
            println("RoomActivationScene Update")
            OperationResult.SUCCESS
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
            OperationResult.SUCCESS
        }
        withCallback {
            println("Room1DeactivationScene Finished")
        }
    }
}

fun main(args: Array<String>) {
    DesktopApp("TiledTileMapTest", 640, 640) {

        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        // engine init
        TestGameObject
        Movement
//        TileMapSystem
//        MovementSystem
//        ContactSystem
//        TestGameObject
//        WorldSystem

        // game init
        initControl()
        initView()
        initScenes()
        // player
        initPlayerTasks()
        val playerId = Player {
            name = "player1"
            onLoadTask("LoadPlayer")
            withControl(PlatformerHMoveController) {
                updateResolution = 20f
                inputDevice = Engine.input.getDevice("INPUT_DEVICE_KEY1")
            }
            withControl(PlatformerJumpController) {
                doubleJump = true
                inputDevice = Engine.input.getDevice("INPUT_DEVICE_KEY1")
            }
        }

        // area init
        initAreaTasks()
        initRoomTasks()
        val areaId = AreaJsonAsset {
            name = "TiledMapTestAreaAsset"
            resourceName = "tiles/testArea.json"
            autoBuildAreaComponent = true
        }

        // load
        AreaJsonAsset.load(areaId)
        // activate area
        Area.activate("TiledMapTestArea")
        // start game in Room1
        Room.startRoom(playerId.instanceIndex, 4 * 16, 4 * 16, Room["Room1"].index)
    }
}


class TestGameObject : Composite(TestGameObject) {

    private var spriteId = NO_COMPONENT_KEY
    private var entityId = NO_COMPONENT_KEY

    override fun load() {
        super.load()
        spriteId = Sprite {
            autoActivation = true
            name = "objectSprite"
            textureRef("tileSetAtlas_bluTerrain_tileSetAsset")
            textureRegion(7 * 16, 1 * 16, 16, 16)
        }
    }

    override fun activate() {
        super.activate()
        entityId = Entity {
            autoActivation = true
            withComponent(ETransform) {
                viewRef(this@TestGameObject.viewRef)
                layerRef(this@TestGameObject.layerRef)
                position(this@TestGameObject.position)
            }
            withComponent(ESprite) {
                spriteRef(this@TestGameObject.spriteId)
                blendMode = BlendMode.NORMAL_ALPHA
            }
            withComponent(EContact) {
                contactBounds(0, 0, 16, 16)
                contactType = TileContactFormType.QUAD
                material = TileMaterialType.TERRAIN_SOLID
            }
        }
    }

    override fun deactivate() {
        super.deactivate()
        Entity.deactivate(entityId)
    }

    override fun dispose() {
        super.dispose()
        Entity.dispose(entityId)
    }

    companion object : ComponentSubTypeBuilder<Composite, TestGameObject>(Composite, "TestGameObject") {
        override fun create() = TestGameObject()
    }
}
