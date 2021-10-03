package com.inari.firefly.core.api

import com.inari.firefly.ZERO_FLOAT
import com.inari.util.geom.PositionF
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class TransformData constructor(
    @JvmField val position: PositionF = PositionF(ZERO_FLOAT, ZERO_FLOAT),
    @JvmField val pivot: PositionF = PositionF(ZERO_FLOAT, ZERO_FLOAT),
    @JvmField val scale: Vector2f = Vector2f(1.0f, 1.0f),
    @JvmField var rotation: Float = ZERO_FLOAT
) {
    val hasRotation: Boolean get() = rotation != 0f
    val hasScale: Boolean get() = scale.dx != 1.0f || scale.dy != 1.0f

    operator fun plus(td: TransformData): TransformData {
        position + td.position
        pivot + td.pivot
        scale * td.scale
        rotation += td.rotation
        return this
    }

    fun reset() {
        position.x = ZERO_FLOAT
        position.y = ZERO_FLOAT
        pivot.x = ZERO_FLOAT
        pivot.y = ZERO_FLOAT
        scale.dx = 1.0f
        scale.dy = 1.0f
        rotation = ZERO_FLOAT
    }
}
