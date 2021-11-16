package com.inari.firefly.graphics.rendering

import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.shape.EShape
import com.inari.util.geom.Vector4i

class SimpleShapeRenderer private constructor() : Renderer() {

    override fun match(entity: Entity): Boolean =
        EShape in entity.aspects

    override fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) {
        val toRender = getIfNotEmpty(viewIndex, layerIndex) ?: return

        val graphics = FFContext.graphics
        var i = 0
        while (i < toRender.capacity) {
            val entity = toRender[i++] ?: continue
            graphics.renderShape(
                entity[EShape].data,
                entity[ETransform].data
            )
        }
    }

    override fun componentType() = Companion
    companion object : SingletonComponent<Renderer, SimpleShapeRenderer>(Renderer, SimpleShapeRenderer::class) {
        override fun create() = SimpleShapeRenderer()
    }
}