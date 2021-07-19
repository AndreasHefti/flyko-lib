package com.inari.firefly.filter

import com.badlogic.gdx.graphics.Pixmap
import com.inari.util.IntFunction

class ColorFilteredTextureData(
    resourcePath: String,
    private val colorConverter: IntFunction
) : FilteredTextureData(resourcePath) {
    override fun applyFilter(pixmap: Pixmap) {
        for (y in 0 until pixmap.height) {
            for (x in 0 until pixmap.width) {
                pixmap.drawPixel(x, y, colorConverter(pixmap.getPixel(x, y)))
            }
        }
    }
}