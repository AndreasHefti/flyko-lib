package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.ai.behavior.ActionNode
import com.inari.firefly.ai.behavior.EBehavior
import com.inari.firefly.ai.behavior.ParallelNode
import com.inari.firefly.ai.behavior.SequenceNode
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.*
import com.inari.firefly.core.api.ActionResult.*
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.EMovement
import kotlin.random.Random

fun main(args: Array<String>) {

    DesktopApp( "CoverCodeTest", 800, 600, debug = true) {

        val now = System.currentTimeMillis()

        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        Texture.build {
            name = "nanoTexture"
            resourceName = "firefly/logo.png"
            withSprite {
                name = "nanoSprite"
                textureBounds(0, 0, 32, 32)
                hFlip = false; vFlip = false
            }
        }

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


        Entity.setMinCapacity(100000)
        val vert = floatArrayOf(0f, 0f, 10f, 20f)
        for (i in 1..10000) {
            Entity {
                autoActivation = true
                withComponent(ETransform) {
                    viewRef(0)
                    position(Random.nextInt(0,800), Random.nextInt(0,600))
                }
                withComponent(ESprite) {
                    spriteRef("nanoSprite")
                }
//                withComponent(EShape) {
//                    type = ShapeType.RECTANGLE
//                    fill = true
//                    segments = 10
//                    color1(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
//                    vertices = vert
//                    blend = BlendMode.NORMAL_ALPHA
//                }
                withComponent(EMovement) {
                    velocity.x = 0f
                    //updateResolution = 1f
                }
                withComponent(EBehavior) {
                    behaviorTreeRef("Root")
                    repeat = true
                    updateResolution =  1f
                }
            }
        }

        Entity.optimize()

        println("************** tuck: ${ System.currentTimeMillis() - now}")
    }
}