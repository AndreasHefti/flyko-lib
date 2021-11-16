package com.inari.firefly.util.graphics

import com.inari.util.geom.GeomUtils.colorOf
import com.inari.util.geom.GeomUtils.hasAlpha
import com.inari.util.geom.GeomUtils.rgB8888
import com.inari.util.geom.GeomUtils.rgbA8888
import com.inari.util.geom.Vector4f
import kotlin.test.*


class ColorTest {

    @Test
    fun testCreation() {
        var color = Vector4f()
        assertEquals("[r=0.0,g=0.0,b=0.0,a=0.0]", color.toString())
        assertTrue(hasAlpha(color))
        color = Vector4f(.5f, .3f, .4f)
        assertEquals("[r=0.5,g=0.3,b=0.4,a=0.0]", color.toString())
        assertTrue(hasAlpha(color))
        color = Vector4f(.5f, .3f, .4f, .6f)
        assertEquals("[r=0.5,g=0.3,b=0.4,a=0.6]", color.toString())
        // out of range --> range correction
        color = colorOf(500.5f, -.3f, .4f, .6f)
        assertEquals("[r=1.0,g=0.0,b=0.4,a=0.6]", color.toString())

        color = colorOf(100, 100, 200)
        assertEquals("[r=0.39215687,g=0.39215687,b=0.78431374,a=1.0]", color.toString())
        // out of range --> range correction
        color = colorOf(100, -100, 500, 20)
        assertEquals("[r=0.39215687,g=0.0,b=1.0,a=0.078431375]", color.toString())

        val color2 = Vector4f(color)
        assertEquals(color2, color)
        assertNotEquals(color2, Vector4f())
    }


    @Test
    fun testRGBA8888() {
        val color = Vector4f(.5f, .3f, .4f, .6f)
        assertEquals("2135713535", rgB8888(color).toString())
        assertEquals("2135713433", rgbA8888(color).toString())
    }

    @Test
    fun testFromHexString() {
        val color1 = colorOf("#AABBCC12")
        assertEquals("[r=0.6666667,g=0.73333335,b=0.8,a=0.07058824]", color1.toString())
        val color2 = colorOf("FFFFFF")
        assertEquals("[r=1.0,g=1.0,b=1.0,a=1.0]", color2.toString())
        val color3 = colorOf("#101010")
        assertEquals("[r=0.0627451,g=0.0627451,b=0.0627451,a=1.0]", color3.toString())
    }

}
