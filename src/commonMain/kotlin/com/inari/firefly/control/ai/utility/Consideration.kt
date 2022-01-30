package com.inari.firefly.control.ai.utility

import com.inari.firefly.control.NormalOperation
import com.inari.firefly.control.ZERO_OP
import com.inari.firefly.control.invoke
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.geom.Easing
import com.inari.util.geom.EasingFunction
import kotlin.jvm.JvmField

class Consideration private constructor() : SystemComponent(Consideration::class.simpleName!!) {

    @JvmField val weighting = 1f
    @JvmField val quantifier: EasingFunction = Easing.LINEAR
    @JvmField var normalOperation: NormalOperation = ZERO_OP

     fun getUtilityValue(entityId: Int, intentionId: Int): Float =
        quantifier(normalOperation(entityId, intentionId)) * weighting

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<Consideration>(Consideration::class) {
        override fun createEmpty() = Consideration()
    }
}