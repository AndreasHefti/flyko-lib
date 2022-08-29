package com.inari.firefly.graphics.view

import com.inari.firefly.GraphicsMock
import com.inari.firefly.TestApp
import com.inari.firefly.graphics.view.View.Companion.BASE_VIEW_KEY
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ViewSystemTest {
 
    @BeforeTest
    fun init() {
        TestApp
        GraphicsMock.clearLogs()
        View.clearSystem()
    }

    @Test
    fun testSystemInit() {

        assertTrue(View.exists(BASE_VIEW_KEY))
        assertTrue(GraphicsMock._views.size == 1)
    }

    @Test
    fun testInitWithController() {
        View.build {
            name = "test"
            withControl(SimpleCameraController) {
                name = "test"
            }
        }
    }
}