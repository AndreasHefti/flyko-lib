package com.inari.firefly.game.world

import com.inari.firefly.TestApp
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.view.ETransform
import org.junit.Test
import kotlin.test.*

class RoomCrossingTest {

    @BeforeTest
    fun init() {
        TestApp
        ComponentSystem.clearSystems()
    }

    @AfterTest
    fun cleanup() {
        ComponentSystem.clearSystems()
    }

    @Test
    fun worldTest1() {

        val playerKey = Player {
            name = "player"
            withLoadTask {
                name = "PlayerLoadTask"
                withVoidOperation {
                    Entity.build {
                        name = "player"
                        withComponent(ETransform) {
                            position(100, 150)
                            pivot(5, 5)
                        }
                    }
                }
            }
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
        val worldId = Area {
            name = "World1"
            withChild(Area) {
                name = "TestArea1"
                withChild(Room) {
                    name = "Room1"
                    roomOrientation(0, 0, 100, 200)
                    areaOrientation(0, 0, 1, 2)
                }
                withChild(Room) {
                    name = "Room2"
                    roomOrientation(0, 0, 200, 200)
                    areaOrientation(1, 1, 1, 2)
                }
            }
        }

        Room.activate("Room1" )

        assertEquals("Room1", Room.activeRoomKey.name)
        assertTrue(Room["Room1"].active)
        assertFalse(Room["Room2"].active)
        assertTrue(Area["TestArea1"].loaded)
        assertTrue(Area["World1"].loaded)

        Player.activate(playerKey)
        assertTrue(Entity["player"].active)
        val p = Player[playerKey]
        assertEquals("100.0,150.0", p.playerPosition.toJsonString())
        assertEquals("5,5", p.playerPivot.toJsonString())

        assertEquals("0", p.playerRoomTransitionObserver.roomX1.toString())
        assertEquals("100", p.playerRoomTransitionObserver.roomX2.toString())
        assertEquals("0", p.playerRoomTransitionObserver.roomY1.toString())
        assertEquals("200", p.playerRoomTransitionObserver.roomY2.toString())

        // simulate room change by players crossing rooms
        p.playerPosition.x += p.playerPivot.v0
        TestApp.update()

        assertEquals( "Room2",  Room.activeRoomKey.name)
        assertTrue(Entity["player"].active)
        assertEquals("-5.0,50.0", p.playerPosition.toJsonString())
        assertEquals("5,5", p.playerPivot.toJsonString())

        assertEquals("0", p.playerRoomTransitionObserver.roomX1.toString())
        assertEquals("200", p.playerRoomTransitionObserver.roomX2.toString())
        assertEquals("0", p.playerRoomTransitionObserver.roomY1.toString())
        assertEquals("200", p.playerRoomTransitionObserver.roomY2.toString())

    }

}