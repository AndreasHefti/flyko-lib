package com.inari.firefly.behavior

import com.inari.firefly.FFContext
import com.inari.firefly.TestApp
import com.inari.firefly.control.FALSE_CONDITION
import com.inari.firefly.control.behavior.*
import com.inari.firefly.control.task.SimpleTask
import com.inari.firefly.control.task.TaskSystem
import kotlin.test.BeforeTest
import kotlin.test.Test

class BehaviorTreeTest {

    @BeforeTest
    fun init() {
        TestApp
        BehaviorSystem.clearSystem()
        TaskSystem.clearSystem()
    }

    @Test
    fun testCreation() {

        SimpleTask.build {
            name = "Task_Name"
            withSimpleOperation {  }
        }

        BxSelection.build {
            name = "First Selection"
            node(BxSelection) {
                name = "Second Sele ction"
                node(BxCondition) {
                    name ="Condition 1"
                    condition = FALSE_CONDITION
                }
                node(BxAction) {
                    name = "First Task"
                    tickOp = { entity, _ -> TaskSystem.runTask("Task_Name", entity.componentId) }
                }
                node(BxSequence) {
                    name = ""
                }

            }
            node(BxSequence) {

            }
        }
    }
}