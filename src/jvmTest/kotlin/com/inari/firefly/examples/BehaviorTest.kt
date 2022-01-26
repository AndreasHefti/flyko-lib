import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.BlendMode
import com.inari.firefly.DesktopApp
import com.inari.firefly.FFContext
import com.inari.firefly.control.ACTION_DONE_CONDITION
import com.inari.firefly.control.BEHAVIOR_STATE_ASPECT_GROUP
import com.inari.firefly.control.OpResult
import com.inari.firefly.control.behavior.*
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
import com.inari.util.aspect.Aspect
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

        val goRightState: Aspect = BEHAVIOR_STATE_ASPECT_GROUP.createAspect("goRight")
        val goLeftState: Aspect = BEHAVIOR_STATE_ASPECT_GROUP.createAspect("goLeft")
        val goDownState: Aspect = BEHAVIOR_STATE_ASPECT_GROUP.createAspect("goDown")
        val goUpState: Aspect = BEHAVIOR_STATE_ASPECT_GROUP.createAspect("goUp")

        BxSequence.build {
            name = "Root"
            node(BxParallel) {
                name = "parallel"
                successThreshold = 2
                node(BxSequence) {
                    name = "X"
                    node(BxSelection) {
                        name = "right"
                        node(BxCondition) {
                            name = "GoRight done?"
                            condition = ACTION_DONE_CONDITION(goRightState)
                        }
                        node(BxAction) {
                            name="GoRight"
                            state = goRightState
                            tickOp = { entity, _ ->
                                val mov = entity[EMovement]
                                if (mov.velocityX <= 0f)
                                    mov.velocityX = Random.nextInt(1, 5).toFloat()
                                if (entity[ETransform].position.x < 800f)
                                    OpResult.RUNNING
                                else
                                    OpResult.SUCCESS
                            }
                        }
                    }
                    node(BxAction) {
                        name="GoLeft"
                        state = goLeftState
                        tickOp = { entityId, bx ->
                            val mov = entityId[EMovement]
                            if (mov.velocityX >= 0f)
                                mov.velocityX = Random.nextInt(-5, -1).toFloat()
                            if (entityId[ETransform].position.x < 10f) {
                                bx.actionsDone - goRightState
                                OpResult.SUCCESS
                            }
                            else
                                OpResult.RUNNING
                        }
                    }
                }
                node(BxSequence) {
                    name = "Y"
                    node(BxSelection) {
                        name = "down"
                        node(BxCondition) {
                            name = "GoDown done?"
                            condition = ACTION_DONE_CONDITION(goDownState)
                        }
                        node(BxAction) {
                            name="GoDown"
                            state = goDownState
                            tickOp = { entityId, _ ->
                                val mov = entityId[EMovement]
                                if (mov.velocityY <= 0f)
                                    mov.velocityY = Random.nextInt(1, 5).toFloat()
                                if (entityId[ETransform].position.y < 600)
                                    OpResult.RUNNING
                                else
                                    OpResult.SUCCESS
                            }
                        }
                    }
                    node(BxAction) {
                        name="GoUp"
                        state = goUpState
                        tickOp = { entityId, bx ->
                            val mov = entityId[EMovement]
                            if (mov.velocityY >= 0f)
                                mov.velocityY = Random.nextInt(-5, -1).toFloat()
                            if (entityId[ETransform].position.y < 10) {
                                bx.actionsDone - goDownState
                                OpResult.SUCCESS
                            }
                            else
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
                    updateResolution = 30f
                }
                withComponent(EBehavior) {
                    behaviorTree("Root")
                    repeat = true
                    updateResolution = 30f
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
                Lwjgl3Application(BehaviorTest(), config)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}