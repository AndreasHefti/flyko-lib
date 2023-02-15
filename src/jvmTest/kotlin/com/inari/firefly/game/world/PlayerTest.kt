package com.inari.firefly.game.world

import com.inari.firefly.TestApp
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.core.Entity
import com.inari.firefly.game.player.Player
import kotlin.test.*


class PlayerTest {

    @BeforeTest
    fun init() {
        TestApp
        ComponentSystem.clearSystems()
    }

    @AfterTest fun cleanup() {
        ComponentSystem.clearSystems()
    }

    @Test
    @Ignore
    fun testCreationWithLoosReference() {
        TestApp
        ComponentSystem.clearSystems()

        val playerComposite = Player.buildAndGet {
            name = "player"
        }

        assertEquals("player", playerComposite.name)
        assertEquals("CKey(player, Entity, -1)", playerComposite.playerEntityKey.toString())

        // now create the entity with the name playerEntity
        Entity.build {
            name = "player"
        }

        assertEquals("CKey(player, Entity, 0)", playerComposite.playerEntityKey.toString())

        // now try tp create another with the same name
        try {
            Entity.build {
                name = "player"
            }
            fail("Error expected here")
        } catch (e: Exception) {
            assertEquals("Key with same name already exists", e.message)
        }
    }

}