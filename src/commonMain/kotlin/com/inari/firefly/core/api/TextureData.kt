package com.inari.firefly.core.api

import com.inari.firefly.NO_NAME
import com.inari.firefly.NULL_INT_FUNCTION
import com.inari.util.IntFunction
import kotlin.jvm.JvmField

class TextureData(
    @JvmField internal var resourceName: String = NO_NAME,
    @JvmField internal var isMipmap: Boolean = false,
    @JvmField internal var wrapS: Int = -1,
    @JvmField internal var wrapT: Int = -1,
    @JvmField internal var minFilter: Int = -1,
    @JvmField internal var magFilter: Int = -1,
    @JvmField internal var colorConverter: IntFunction = NULL_INT_FUNCTION
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
