package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.ai.behavior.ActionNode
import com.inari.firefly.ai.behavior.EBehavior
import com.inari.firefly.ai.behavior.ParallelNode
import com.inari.firefly.ai.behavior.SequenceNode
import com.inari.firefly.core.*
import com.inari.firefly.core.Component.Companion.COMPONENT_GROUP_ASPECT
import com.inari.firefly.core.api.*
import com.inari.firefly.core.api.ActionResult.*
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.aspect.Aspect
import kotlin.random.Random

fun main(args: Array<String>) {
    DesktopApp( "CoverCodeTest", 800, 600, debug = true) {

        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        SequenceNode.build {
            name = "Root"
            node(ParallelNode) {
                name = "parallel"
                successThreshold = 1
                node(SequenceNode) {
                    name = "X"
                    node(ActionNode) {
                        name="GoRight"
                        actionOperation = object : Action {
                            override fun invoke(index: EntityIndex): ActionResult {
                                val mov = EMovement[index]
                                return if (mov.velocity.x < 0)
                                    SUCCESS
                                else if (ETransform[index].position.x > 800f || mov.velocity.x == 0.0f) {
                                    mov.velocity.x = Random.nextInt(-150, -50).toFloat()
                                    SUCCESS
                                } else
                                    RUNNING
                            }
                        }
                    }
                    node(ActionNode) {
                        name="GoLeft"
                        actionOperation = object : Action {
                            override fun invoke(index: EntityIndex): ActionResult {
                                val mov = EMovement[index]
                                return if (mov.velocity.x > 0)
                                    SUCCESS
                                else if (ETransform[index].position.x < 10f) {
                                    mov.velocity.x = Random.nextInt(50, 150).toFloat()
                                    SUCCESS
                                } else
                                    RUNNING
                            }
                        }
                    }
                }
                node(SequenceNode) {
                    name = "Y"
                    node(ActionNode) {
                        name="GoDown"
                        actionOperation = object : Action {
                            override fun invoke(index: EntityIndex): ActionResult {
                                val mov = EMovement[index]
                                return if (mov.velocity.y < 0)
                                    SUCCESS
                                else if (ETransform[index].position.y > 600f || mov.velocity.y == 0.0f) {
                                    mov.velocity.y = Random.nextInt(-150, -50).toFloat()
                                    SUCCESS
                                } else
                                    RUNNING
                            }
                        }
                    }
                    node(ActionNode) {
                        name="GoUp"
                        actionOperation = object : Action {
                            override fun invoke(index: EntityIndex): ActionResult {
                                val mov = EMovement[index]
                                return if (mov.velocity.y > 0)
                                    SUCCESS
                                else if (ETransform[index].position.y < 10f) {
                                    mov.velocity.y = Random.nextInt(50, 150).toFloat()
                                    SUCCESS
                                } else
                                    RUNNING
                            }
                        }
                    }
                }
            }
        }

        val vert = floatArrayOf(0f, 0f, 10f, 20f)
        for (i in 1..10000) {
            Entity {
                // Two different groups for pausing
                groups + if (i % 2 != 0) "even" else "odd"
                autoActivation = true
                withComponent(ETransform) {
                    viewRef(0)
                    position(Random.nextInt(0,800), Random.nextInt(0,600))
                }
                withComponent(EShape) {
                    type = ShapeType.RECTANGLE
                    fill = true
                    segments = 10
                    color1(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
                    vertices = vert
                    blend = BlendMode.NORMAL_ALPHA
                }
                withComponent(EMovement) {
                    velocity.x = 0f
                    updateResolution = 100f
                }
                withComponent(EBehavior) {
                    behaviorTreeRef("Root")
                    repeat = true
                    updateResolution =  30f
                }
            }
        }

        UpdateControl {
            autoActivation = true
            updateResolution = 10f
            var timer = 0
            var even = true
            updateOp = {
                timer++
                if (timer > 100) {
                    timer = 0
                    even = !even
                    println("*** send Pause event")
                    val aspect: Aspect =
                        if (even) COMPONENT_GROUP_ASPECT["even"]!!
                        else COMPONENT_GROUP_ASPECT["odd"]!!

                    val groupsToPause = COMPONENT_GROUP_ASPECT.createAspects()
                    groupsToPause + aspect
                    Pausing.pauseAllExclusive(groupsToPause)
                }
            }
        }
    }
}