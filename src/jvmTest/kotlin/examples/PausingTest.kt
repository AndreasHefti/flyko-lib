package examples.game

import com.inari.firefly.DesktopApp
import com.inari.firefly.ai.behavior.ActionNode
import com.inari.firefly.ai.behavior.EBehavior
import com.inari.firefly.ai.behavior.ParallelNode
import com.inari.firefly.ai.behavior.SequenceNode
import com.inari.firefly.core.*
import com.inari.firefly.core.api.OperationResult.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ShapeType
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
                        actionOperation = { entityId ->
                            val entity = Entity[entityId]
                            val mov = entity[EMovement]
                            if (mov.velocity.x < 0)
                                SUCCESS
                            else if (entity[ETransform].position.x > 800f || mov.velocity.x == 0.0f) {
                                mov.velocity.x = Random.nextInt(-150, -50).toFloat()
                                SUCCESS
                            } else
                                RUNNING
                        }
                    }
                    node(ActionNode) {
                        name="GoLeft"
                        actionOperation =  { entityId ->
                            val entity = Entity[entityId]
                            val mov = entity[EMovement]
                            if (mov.velocity.x > 0)
                                SUCCESS
                            else if (entity[ETransform].position.x < 10f) {
                                mov.velocity.x = Random.nextInt(50, 150).toFloat()
                                SUCCESS
                            } else
                                RUNNING
                        }
                    }
                }
                node(SequenceNode) {
                    name = "Y"
                    node(ActionNode) {
                        name="GoDown"
                        actionOperation =  { entityId ->
                            val entity = Entity[entityId]
                            val mov = entity[EMovement]
                            if (mov.velocity.y < 0)
                                SUCCESS
                            else if (entity[ETransform].position.y > 600f || mov.velocity.y == 0.0f) {
                                mov.velocity.y = Random.nextInt(-150, -50).toFloat()
                                SUCCESS
                            } else
                                RUNNING
                        }
                    }
                    node(ActionNode) {
                        name="GoUp"
                        actionOperation =  { entityId ->
                            val entity = Entity[entityId]
                            val mov = entity[EMovement]
                            if (mov.velocity.y > 0)
                                SUCCESS
                            else if (entity[ETransform].position.y < 10f) {
                                mov.velocity.y = Random.nextInt(50, 150).toFloat()
                                SUCCESS
                            } else
                                RUNNING
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
            updateOp =  {
                timer++
                if (timer > 100) {
                    timer = 0
                    even = !even
                    println("*** send Pause event")
                    val aspect: Aspect =
                        if (even) ComponentGroups.COMPONENT_GROUP_ASPECT["even"]!!
                        else ComponentGroups.COMPONENT_GROUP_ASPECT["odd"]!!

                    val groupsToPause = ComponentGroups.COMPONENT_GROUP_ASPECT.createAspects()
                    groupsToPause + aspect
                    PauseEvent.notifyPause(groupsToPause)
                }
            }
        }
    }


}