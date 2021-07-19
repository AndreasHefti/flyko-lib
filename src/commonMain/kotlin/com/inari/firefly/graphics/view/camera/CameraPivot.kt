package com.inari.firefly.graphics.view.camera

import com.inari.util.geom.PositionF

interface CameraPivot {
    fun init()
    operator fun invoke(): PositionF
}