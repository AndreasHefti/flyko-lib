package com.inari.firefly.graphics.view

import com.inari.firefly.GraphicsMock
import com.inari.firefly.TestApp
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ViewSystemTest {

    @BeforeTest
    fun init() {
        GraphicsMock.clearLogs()
        ViewSystem.clearSystem()
    }

    @Test
    fun testSystemInit() {
        TestApp
        ViewSystem

        assertNotNull(ViewSystem.baseView)
        assertTrue(GraphicsMock._views.size == 1)
    }
}