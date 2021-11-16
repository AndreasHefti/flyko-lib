package com.inari.firefly.misc

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.collection.DynIntArray
import com.inari.util.geom.Vector2i

data class Test2Component constructor (
    var Param1: String,
    var Param2: Int,
    var Param3: Vector2i,
    var Param4: DynIntArray
) : SystemComponent(Test2Component::class.simpleName!!) {

    private constructor() : this("Param1", 0, Vector2i(), DynIntArray(5, -1, 5))

    override fun componentType() =
        Test2Component.Companion

    companion object : SystemComponentSingleType<Test2Component>(Test2Component::class) {
        override fun createEmpty(): Test2Component = Test2Component()
    }

}