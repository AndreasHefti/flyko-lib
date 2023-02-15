package com.inari.firefly.game.room

import com.inari.firefly.core.*

class Room private constructor() : Composite(Room) {


    companion object : ComponentSubTypeBuilder<Composite, Room>(Composite,"Room") {
        override fun create() = Room()

        const val ATTR_OBJECT_ID = "id"
        const val ATTR_OBJECT_TYPE = "type"
        const val ATTR_OBJECT_NAME = "name"
        const val ATTR_OBJECT_BOUNDS = "bounds"
        const val ATTR_OBJECT_ROTATION = "rotation"
        const val ATTR_OBJECT_VISIBLE = "visible"
        const val ATTR_OBJECT_GROUPS = "groups"
    }
}



