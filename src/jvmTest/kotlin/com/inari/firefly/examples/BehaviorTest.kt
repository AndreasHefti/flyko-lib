import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.BlendMode
import com.inari.firefly.DesktopApp
import com.inari.firefly.FFContext
import com.inari.firefly.control.OpResult
import com.inari.firefly.control.ai.behavior.*
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.rendering.RenderingSystem
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.info.FFInfoSystem
import com.inari.firefly.info.FrameRateInfo
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.MovementSystem
import kotlin.random.Random

class BehaviorTest : DesktopApp() {

    override val title: String = "BehaviorTest"

    override fun init() {
        FFInfoSystem
                .addInfo(FrameRateInfo)
                .activate()
        RenderingSystem
        FFContext.loadSystem(EntitySystem)
        FFContext.loadSystem(BehaviorSystem)
        FFContext.loadSystem(TaskSystem)
        MovementSystem

        BxSequence.build {
            name = "Root"
            node(BxParallel) {
                name = "parallel"
                successThreshold = 1
                node(BxSequence) {
                    name = "X"
                    node(BxAction) {
                        name="GoRight"
                        action = { entityId, _, _ ->
                            val entity = EntitySystem[entityId]
                            val mov = entity[EMovement]
                            if (mov.velocityX < 0)
                                OpResult.SUCCESS
                            else if (entity[ETransform].position.x > 800f || mov.velocityX == 0.0f) {
                                mov.velocityX = Random.nextInt(-150, -50).toFloat()
                                OpResult.SUCCESS
                            } else
                                OpResult.RUNNING
                        }
                    }
                    node(BxAction) {
                        name="GoLeft"
                        action = { entityId, _, _ ->
                            val entity = EntitySystem[entityId]
                            val mov = entity[EMovement]
                            if (mov.velocityX > 0)
                                OpResult.SUCCESS
                            else if (entity[ETransform].position.x < 10f) {
                                mov.velocityX = Random.nextInt(50, 150).toFloat()
                                OpResult.SUCCESS
                            } else
                                OpResult.RUNNING
                        }
                    }
                }
                node(BxSequence) {
                    name = "Y"
                    node(BxAction) {
                        name="GoDown"
                        action = { entityId, _, _ ->
                            val entity = EntitySystem[entityId]
                            val mov = entity[EMovement]
                            if (mov.velocityY < 0)
                                OpResult.SUCCESS
                            else if (entity[ETransform].position.y > 600f || mov.velocityY == 0.0f) {
                                mov.velocityY = Random.nextInt(-150, -50).toFloat()
                                OpResult.SUCCESS
                            } else
                                OpResult.RUNNING
                        }
                    }
                    node(BxAction) {
                        name="GoUp"
                        action = { entityId, _, _ ->
                            val entity = EntitySystem[entityId]
                            val mov = entity[EMovement]
                            if (mov.velocityY > 0)
                                OpResult.SUCCESS
                            else if (entity[ETransform].position.y < 10f) {
                                mov.velocityY = Random.nextInt(50, 150).toFloat()
                                OpResult.SUCCESS
                            } else
                                OpResult.RUNNING
                        }
                    }
                }
            }
        }

        val vert = floatArrayOf(0f, 0f, 3f, 3f)
        for (i in 1..10000) {
            Entity.buildAndActivate {
                withComponent(ETransform) {
                    view(0)
                    position(Random.nextInt(0,800), Random.nextInt(0,600))
                }
                withComponent(EShape) {
                    shapeType = ShapeType.RECTANGLE
                    fill = true
                    segments = 10
                    color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
                    vertices = vert
                    blend = BlendMode.NORMAL_ALPHA
                }
                withComponent(EMovement) {
                    velocityX = 0f
                    updateResolution = 100f
                }
                withComponent(EBehavior) {
                    behaviorTree("Root")
                    repeat = true
                    updateResolution = 5f
                }
            }
        }
    }

    companion object {
        @JvmStatic fun main(arg: Array<String>) {
            try {
                val config = Lwjgl3ApplicationConfiguration()
                config.setResizable(true)
                config.setWindowedMode(800, 600)
                config.useVsync(true)
                Lwjgl3Application(BehaviorTest(), config)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}