package com.inari.firefly.util.geom

import com.inari.util.geom.GeomUtils
import com.inari.util.geom.GeomUtils.bezierCurveAngleX
import com.inari.util.geom.GeomUtils.bezierCurvePoint
import com.inari.util.geom.Vector2f
import kotlin.test.Test
import kotlin.test.assertEquals

class GeomUtilsTest {

    @Test
    fun intoBoundary() {
        assertEquals(.5f, GeomUtils.intoBoundary(.5f,0f, 1f))
        assertEquals(0f, GeomUtils.intoBoundary(-.5f,0f, 1f))
        assertEquals(1f, GeomUtils.intoBoundary(2.5f,0f, 1f))

        assertEquals(5, GeomUtils.intoBoundary(5,0, 10))
        assertEquals(0, GeomUtils.intoBoundary(-5,0, 10))
        assertEquals(10, GeomUtils.intoBoundary(25,0, 10))
    }

    @Test
    fun testBezierCurvePoint() {
        val v0 = Vector2f(0f, 0f)
        val v1 = Vector2f(0f,10f)
        val v2 = Vector2f(10f,10f)
        val v3 = Vector2f(10f,0f)

        assertEquals(Vector2f(0f, 0f), bezierCurvePoint(v0,v1,v2,v3, 0f))
        assertEquals(Vector2f(1.5625f, 5.625f), bezierCurvePoint(v0,v1,v2,v3, .25f))
        assertEquals(Vector2f(5f, 7.5f), bezierCurvePoint(v0,v1,v2,v3, .5f))
        assertEquals(Vector2f(8.4375f, 5.625f), bezierCurvePoint(v0,v1,v2,v3, .75f))
        assertEquals(Vector2f(10f, 0f), bezierCurvePoint(v0,v1,v2,v3, 1f))
    }

    @Test
    fun testBezierCurveAngleX() {
        val v0 = Vector2f(0f, 0f)
        val v1 = Vector2f(0f,10f)
        val v2 = Vector2f(10f,10f)
        val v3 = Vector2f(10f,0f)

        assertEquals(1.5707964f, bezierCurveAngleX(v0,v1,v2,v3, 0f))
        assertEquals(0.9272952f, bezierCurveAngleX(v0,v1,v2,v3, .25f))
        assertEquals(0f, bezierCurveAngleX(v0,v1,v2,v3, .5f))
        assertEquals(-0.9272952f, bezierCurveAngleX(v0,v1,v2,v3, .75f))
        assertEquals(-1.5707964f, bezierCurveAngleX(v0,v1,v2,v3, 1f))
    }

    @Test
    fun testAngleX() {
        assertEquals(0f, GeomUtils.angleX(Vector2f(1f, 0f)))
        assertEquals(0.7853982f, GeomUtils.angleX(Vector2f(.5f, .5f)))
        assertEquals(1.5707964f, GeomUtils.angleX(Vector2f(0f, 1f)))
    }
    @Test
    fun testAngleY() {
        assertEquals(1.5707964f, GeomUtils.angleY(Vector2f(1f, 0f)))
        assertEquals(0.7853982f, GeomUtils.angleY(Vector2f(.5f, .5f)))
        assertEquals(0f, GeomUtils.angleY(Vector2f(0f, 1f)))
    }
 }