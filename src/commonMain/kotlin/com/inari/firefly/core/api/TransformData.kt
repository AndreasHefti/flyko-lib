package com.inari.firefly.core.api

import com.inari.firefly.ZERO_FLOAT
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class TransformData constructor(
    @JvmField internal val position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT),
    @JvmField internal val pivot: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT),
    @JvmField internal val scale: Vector2f = Vector2f(1.0f, 1.0f),
    @JvmField internal var rotation: Float = ZERO_FLOAT
) {
    val hasRotation: Boolean get() = rotation != 0f
    val hasScale: Boolean get() = scale.v0 != 1.0f || scale.v1 != 1.0f

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
        scale.v0 = 1.0f
        scale.v1 = 1.0f
        rotation = ZERO_FLOAT
    }
}
