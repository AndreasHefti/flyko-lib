package com.inari.firefly.game.world

import com.inari.firefly.FFContext
import com.inari.firefly.TestApp
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.control.task.SimpleTask
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.game.player.PlayerComposite
import com.inari.firefly.game.player.PlayerSystem
import com.inari.firefly.graphics.ETransform
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomCrossingTest {

    @Test
    fun worldTest1() {
        TestApp
        EntitySystem
        WorldSystem
        TaskSystem
        PlayerSystem
            .withPlayerLoadTask(SimpleTask) {
                name = "PlayerLoadTask"
                withSimpleOperation {
                    Entity.build {
                        name = PlayerComposite.PLAYER_ENTITY_NAME
                        withComponent(ETransform) {
                            position(100, 150)
                            pivot(5, 5)
                        }
                    }
                }
            }

        val worldId = World.build {
            name = "World1"
        }
        val areaId = Area.build {
            name = "TestArea1"
            parentName = "World1"
        }

        // room1  room2
        // +----+
        // |    |
        // |    |
        // |    |+------+
        // |   p-->     |
        // +----+|      |
        //       |      |
        //       |      |
        //       +------+

        val roomId1 = Room.build {
            name = "Room1"
            parentName = "TestArea1"
            roomOrientation(0, 0, 100, 200)
            areaOrientation(0, 0, 1, 2)
        }
        val roomId2 = Room.build {
            name = "Room2"
            parentName = "TestArea1"
            roomOrientation(0, 0, 200, 200)
            areaOrientation(1, 1, 1, 2)
        }

        FFContext.activate(roomId1)

        assertEquals(roomId1, WorldSystem.activeRoomId)
        assertTrue(FFContext.isActive(roomId1))
        assertFalse(FFContext.isActive(roomId2))
        assertTrue(FFContext.isLoaded(areaId))
        assertTrue(FFContext.isLoaded(worldId))

        PlayerSystem.activatePlayer()
        assertTrue { EntitySystem.entities.isActive(PlayerComposite.PLAYER_ENTITY_NAME) }
        assertEquals("100.0,150.0", PlayerSystem.playerPosition.toJsonString())
        assertEquals("0.0,0.0", PlayerSystem.playerVelocity.toJsonString())
        assertEquals("5,5", PlayerSystem.playerPivot.toJsonString())

        assertEquals("0", WorldSystem.RoomPlayerListener.roomX1.toString())
        assertEquals("100", WorldSystem.RoomPlayerListener.roomX2.toString())
        assertEquals("0", WorldSystem.RoomPlayerListener.roomY1.toString())
        assertEquals("200", WorldSystem.RoomPlayerListener.roomY2.toString())

        // simulate room change by players crossing rooms
        PlayerSystem.playerPosition.x += PlayerSystem.playerPivot.v0
        TestApp.update()

        assertEquals( "Room2",  FFContext[Room, WorldSystem.activeRoomId].name)
        assertTrue { EntitySystem.entities.isActive(PlayerComposite.PLAYER_ENTITY_NAME) }
        assertEquals("-5.0,50.0", PlayerSystem.playerPosition.toJsonString())
        assertEquals("0.0,0.0", PlayerSystem.playerVelocity.toJsonString())
        assertEquals("5,5", PlayerSystem.playerPivot.toJsonString())

        assertEquals("0", WorldSystem.RoomPlayerListener.roomX1.toString())
        assertEquals("200", WorldSystem.RoomPlayerListener.roomX2.toString())
        assertEquals("0", WorldSystem.RoomPlayerListener.roomY1.toString())
        assertEquals("200", WorldSystem.RoomPlayerListener.roomY2.toString())

        TestApp.resetTimer()
        CompositeSystem.clearSystem()
        EntitySystem.clearSystem()
    }

}