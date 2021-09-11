package com.inari.firefly.game.camera

import com.inari.util.geom.PositionF

interface CameraPivot {
    fun init()
    operator fun invoke(): PositionF
}