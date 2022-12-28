package com.inari.firefly.graphics.shape

import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynArray

object SimpleShapeRenderer : EntityRenderer("SimpleShapeRenderer") {

    init { order = 40 }

    override fun acceptEntity(entity: Entity) = EShape in entity.aspects

    override fun sort(entities: DynArray<Entity>) {
        // no sorting
    }

    override fun render(entities: DynArray<Entity>) {
        val graphics = Engine.graphics
        var i = 0
        while (i < entities.capacity) {
            val entity = entities[i++] ?: continue
            graphics.renderShape(entity[EShape], entity[ETransform])
        }
    }
}