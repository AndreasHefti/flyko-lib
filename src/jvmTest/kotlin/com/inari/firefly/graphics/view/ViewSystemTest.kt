package com.inari.firefly.graphics.view

import com.inari.firefly.GraphicsMock
import com.inari.firefly.TestApp
import com.inari.firefly.control.ControllerSystem
import com.inari.firefly.game.world.SimpleCameraController
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ViewSystemTest {

    @BeforeTest
    fun init() {
        TestApp
        GraphicsMock.clearLogs()
        ViewSystem.clearSystem()
    }

    @Test
    fun testSystemInit() {
        ViewSystem

        assertNotNull(ViewSystem.baseView)
        assertTrue(GraphicsMock._views.size == 1)
    }

    @Test
    fun testInitWithController() {
        ViewSystem
        ControllerSystem

        View.build {
            name = "test"
            withController(SimpleCameraController) {
                name = "test"
            }
        }
    }
}