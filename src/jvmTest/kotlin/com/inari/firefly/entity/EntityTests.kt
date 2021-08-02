package com.inari.firefly.entity

import com.inari.firefly.FFContext
import com.inari.firefly.TestApp
import com.inari.firefly.graphics.ETransform
import kotlin.test.Test
import kotlin.test.assertEquals

class EntityTests {

    @Test
    fun testEntityName() {
        TestApp
        EntitySystem

        val id = Entity.build {
            name = "testEntity"
            withComponent(ETransform) {
                position(1, 2)
            }
        }

        val entity = FFContext[Entity, id]
        assertEquals("testEntity", entity.name)
    }
 }