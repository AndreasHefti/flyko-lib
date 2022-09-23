package com.inari.firefly.graphics.view

import com.inari.firefly.GraphicsMock
import com.inari.firefly.TestApp
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.core.Control
import com.inari.firefly.graphics.view.View.Companion.BASE_VIEW_KEY
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViewSystemTest {
 
    @BeforeTest
    fun init() {
        TestApp
        GraphicsMock.clearLogs()
        ComponentSystem.clearSystems()
    }

    @Test
    fun testSystemInit() {

        assertTrue(View.exists(BASE_VIEW_KEY))
        assertTrue(GraphicsMock._views.size == 1)
    }

    @Test
    fun testInitWithController() {
        val viewKey = View {
            autoActivation = true
            name = "test"
            withControl(SimpleCameraController) {
                name = "test"
            }
        }

        assertEquals("CKey(test, View, 1)", viewKey.toString())
        val control = Control["test"]
        assertTrue(control.initialized)
        assertTrue(control.loaded)
        assertTrue(control.active)
    }
}