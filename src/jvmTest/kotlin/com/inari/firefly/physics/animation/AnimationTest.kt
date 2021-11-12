package com.inari.firefly.physics.animation

import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.util.PropertyAccessor
import com.inari.util.geom.PositionF
import kotlin.test.Test

class AnimationTest {

    @Test
    fun accessorTest() {
        val entity = Entity.buildAndGet {
            withComponent(ETransform) {

            }
        }
        val transform = entity[ETransform]
        val accessor = transform::position.get()::x

        val pos = PositionF()

        val posAccessor = PropertyAccessor(
            { pos.x },
            { pos.x = it }
        )

        posAccessor.set(1f)
        val posx = posAccessor.get()
    }
}