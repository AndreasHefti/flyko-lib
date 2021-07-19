package com.inari.firefly.core.api

import com.inari.util.geom.PositionF
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class TransformData constructor(
    @JvmField val position: PositionF = PositionF(0.0f, 0.0f),
    @JvmField val pivot: PositionF = PositionF(0.0f, 0.0f),
    @JvmField val scale: Vector2f = Vector2f(1.0f, 1.0f),
    @JvmField var rotation: Float = 0.0f
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
        position.x = 0.0f
        position.y = 0.0f
        pivot.x = 0.0f
        pivot.y = 0.0f
        scale.dx = 1.0f
        scale.dy = 1.0f
        rotation = 0.0f
    }
}
