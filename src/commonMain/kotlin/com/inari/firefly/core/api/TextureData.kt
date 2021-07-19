package com.inari.firefly.core.api

import com.inari.firefly.NO_NAME
import com.inari.firefly.NULL_INT_FUNCTION
import com.inari.util.IntFunction
import kotlin.jvm.JvmField

class TextureData(
    @JvmField var resourceName: String = NO_NAME,
    @JvmField var isMipmap: Boolean = false,
    @JvmField var wrapS: Int = -1,
    @JvmField var wrapT: Int = -1,
    @JvmField var minFilter: Int = -1,
    @JvmField var magFilter: Int = -1,
    @JvmField var colorConverter: IntFunction = NULL_INT_FUNCTION
) {

    fun reset() {
        resourceName = NO_NAME
        isMipmap = false
        wrapS = 0
        wrapT = 0
        minFilter = 0
        magFilter = 0
        colorConverter = NULL_INT_FUNCTION
    }

}
