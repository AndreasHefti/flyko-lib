package com.inari.firefly.graphics.shape

import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynIntArray

object SimpleShapeRenderer : EntityRenderer("SimpleShapeRenderer") {

    init { order = 40 }

    override fun acceptEntity(index: EntityIndex) =  EShape in Entity[index]

    override fun sort(entities: DynIntArray) {
        // no sorting
    }

    val graphics = Engine.graphics
    override fun render(entities: DynIntArray) {
        var i = entities.nextListIndex(0)
        while (i >= ZERO_INT) {
            val index = entities[i]
            graphics.renderShape(EShape[index].renderData, ETransform[index].renderData)
            i = entities.nextListIndex(i + 1)
        }
    }
}