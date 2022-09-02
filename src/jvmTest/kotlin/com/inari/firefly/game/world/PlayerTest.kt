package com.inari.firefly.game.world

import com.inari.firefly.TestApp
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.core.Entity
import kotlin.test.*


class PlayerTest {

    @BeforeTest
    fun init() {
        TestApp
        ComponentSystem.clearSystems()
    }

    @Test
    fun testCreationWithLoosReference() {
        TestApp
        ComponentSystem.clearSystems()

        val playerComposite = Player.buildAndGet {
            name = "player"
            playerEntityRef("playerEntity")
        }

        assertEquals("player", playerComposite.name)
        assertEquals("CKey(playerEntity, Entity, -1)", playerComposite.playerEntityRef.targetKey.toString())
        assertFalse(playerComposite.playerEntityRef.exists)

        // now create the entity with the name playerEntity
        Entity.build {
            name = "playerEntity"
        }

        assertEquals("CKey(playerEntity, Entity, 0)", playerComposite.playerEntityRef.targetKey.toString())
        assertTrue(playerComposite.playerEntityRef.exists)

        // now try tp create another with the same name
        try {
            Entity.build {
                name = "playerEntity"
            }
            fail("Error expected here")
        } catch (e: Exception) {
            assertEquals("Key with same name already exists", e.message)
        }
    }

}