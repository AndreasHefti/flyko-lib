package com.inari.firefly.util.graphics

import com.inari.util.graphics.IColor
import com.inari.util.graphics.IColor.Companion.of
import com.inari.util.graphics.ImmutableColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals


class ColorTest {

    @Test
    fun testCreation() {
        var color: IColor = ImmutableColor()
        assertEquals("[r=0.0,g=0.0,b=0.0,a=1.0]", color.toString())
        assertFalse(color.hasAlpha)
        color = ImmutableColor(.5f, .3f, .4f)
        assertEquals("[r=0.5,g=0.3,b=0.4,a=1.0]", color.toString())
        assertFalse(color.hasAlpha)
        color = ImmutableColor(.5f, .3f, .4f, .6f)
        assertEquals("[r=0.5,g=0.3,b=0.4,a=0.6]", color.toString())
        // out of range --> range correction
        color = ImmutableColor(500.5f, -.3f, .4f, .6f)
        assertEquals("[r=1.0,g=0.0,b=0.4,a=0.6]", color.toString())

        color = of(100, 100, 200)
        assertEquals("[r=0.39215687,g=0.39215687,b=0.78431374,a=1.0]", color.toString())
        // out of range --> range correction
        color = of(100, -100, 500, 20)
        assertEquals("[r=0.39215687,g=0.0,b=1.0,a=0.078431375]", color.toString())

        val color2 = ImmutableColor(color)
        assertEquals(color2, color)
        assertNotEquals(color2, ImmutableColor())
    }


    @Test
    fun testRGBA8888() {
        val color = ImmutableColor(.5f, .3f, .4f, .6f)
        assertEquals("2135713535", color.rgB8888.toString())
        assertEquals("2135713433", color.rgbA8888.toString())
    }

    @Test
    fun testConstants() {
        assertEquals("[r=0.0,g=0.0,b=0.0,a=1.0]", IColor.BLACK.toString())
        assertEquals("[r=0.0,g=0.0,b=1.0,a=1.0]", IColor.BLU.toString())
        assertEquals("[r=0.0,g=1.0,b=0.0,a=1.0]", IColor.GREEN.toString())
        assertEquals("[r=1.0,g=0.0,b=0.0,a=1.0]", IColor.RED.toString())
        assertEquals("[r=1.0,g=1.0,b=1.0,a=1.0]", IColor.WHITE.toString())
    }

}
