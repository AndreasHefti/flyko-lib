package com.inari.util.geom

import com.inari.util.StringUtils
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

class EasingTest {

    @Test
    fun testLinearPositiveAndNegative() {
        var startValue = 0f
        var endValue = 5f
        val duration: Long = 10
        var changeInValue  = endValue - startValue
        val values = ArrayList<Float>()
        for (time in 0..9) {
            val t: Float = time.toFloat() / duration
            values.add(startValue + changeInValue * Easing.Type.LINEAR(t))
        }

        assertEquals(
            "[0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5]",
            values.toString()
        )

        startValue = 5f
        endValue = 0f
        values.clear()

        changeInValue  = endValue - startValue
        val inverse = changeInValue < 0
        if (inverse) {
            changeInValue = abs(changeInValue)
        }

        for (time in 0..9) {
            if (inverse) {
                val t: Float = time.toFloat() / duration
                values.add(startValue - changeInValue * Easing.Type.LINEAR(t))
            }
        }

        assertEquals(
            "[5.0, 4.5, 4.0, 3.5, 3.0, 2.5, 2.0, 1.5, 1.0, 0.5]",
            values.toString()
        )
    }

    @Test
    fun testAll() {
        val startValue = 0f
        val endValue = 5f
        val duration: Long = 10
        var changeInValue: Float  = endValue - startValue

        val values = ArrayList<CharSequence>()
        for (easing in Easing.Type.values()) {
            for (time in 0..9) {
                val t: Float = time.toFloat() / duration.toFloat()
                values.add(StringUtils.formatFloat(changeInValue * easing(t), 5))
            }
        }
//        assertEquals(
//            "[0.00000, 0.50000, 1.00000, 1.50000, 2.00000, 2.50000, 3.00000, 3.50000, 4.00000, 4.50000, " +
//                    "0.00000, 0.05000, 0.20000, 0.45000, 0.80000, 1.25000, 1.80000, 2.45000, 3.20000, 4.05000, " +
//                    "0.00000, 0.95000, 1.80000, 2.55000, 3.20000, 3.75000, 4.20000, 4.55000, 4.80000, 4.95000, " +
//                    "0.00000, 0.10000, 0.40000, 0.90000, 1.60000, 2.50000, 3.40000, 4.10000, 4.60000, 4.90000, " +
//                    "0.00000, 0.00500, 0.04000, 0.13500, 0.32000, 0.62500, 1.08000, 1.71500, 2.56000, 3.64500, " +
//                    "0.00000, 1.35500, 2.44000, 3.28500, 3.92000, 4.37500, 4.68000, 4.86500, 4.96000, 4.99500, " +
//                    "0.00000, 0.02000, 0.16000, 0.54000, 1.28000, 2.50000, 3.72000, 4.46000, 4.84000, 4.98000, " +
//                    "0.00000, 0.00050, 0.00800, 0.04050, 0.12800, 0.31250, 0.64800, 1.20050, 2.04800, 3.28050, " +
//                    "0.00000, 1.71950, 2.95200, 3.79950, 4.35200, 4.68750, 4.87200, 4.95950, 4.99200, 4.99950, " +
//                    "0.00000, 0.00400, 0.06400, 0.32400, 1.02400, 2.50000, 3.97600, 4.67600, 4.93600, 4.99600, " +
//                    "0.00000, 0.00005, 0.00160, 0.01215, 0.05120, 0.15625, 0.38880, 0.84035, 1.63840, 2.95245, " +
//                    "0.00000, 2.04755, 3.36160, 4.15965, 4.61120, 4.84375, 4.94880, 4.98785, 4.99840, 4.99995, " +
//                    "0.00000, 0.00080, 0.02560, 0.19440, 0.81920, 2.50000, 4.18080, 4.80560, 4.97440, 4.99920, " +
//                    "0.00488, 0.00977, 0.01953, 0.03906, 0.07813, 0.15625, 0.31250, 0.62500, 1.25000, 2.50000, " +
//                    "0.00000, 2.50000, 3.75000, 4.37500, 4.68750, 4.84375, 4.92188, 4.96094, 4.98047, 4.99023, " +
//                    "0.00244, 0.00977, 0.03906, 0.15625, 0.62500, 2.50000, 4.37500, 4.84375, 4.96094, 4.99023, " +
//                    "0.00000, 0.06156, 0.24472, 0.54497, 0.95491, 1.46447, 2.06107, 2.73005, 3.45492, 4.21783, " +
//                    "0.00000, 0.78217, 1.54508, 2.26995, 2.93893, 3.53553, 4.04508, 4.45503, 4.75528, 4.93844, " +
//                    "0.00000, 0.12236, 0.47746, 1.03054, 1.72746, 2.50000, 3.27254, 3.96946, 4.52254, 4.87764, " +
//                    "0.00000, 0.02506, 0.10102, 0.23030, 0.41742, 0.66987, 1.00000, 1.42929, 2.00000, 2.82055, " +
//                    "0.00000, 2.17945, 3.00000, 3.57071, 4.00000, 4.33013, 4.58258, 4.76970, 4.89898, 4.97494, " +
//                    "0.00000, 0.05051, 0.20871, 0.50000, 1.00000, 2.50000, 4.00000, 4.50000, 4.79129, 4.94949, " +
//                    "-0.00000, -0.07157, -0.23225, -0.40100, -0.49676, -0.43849, -0.14514, 0.46434, 1.47099, 2.95586, " +
//                    "0.00000, 2.04414, 3.52901, 4.53566, 5.14514, 5.43849, 5.49676, 5.40100, 5.23225, 5.07157, " +
//                    "0.00000, 0.05938, 0.30000, 0.34688, 1.13750, 1.17188, 0.45000, 1.59687, 3.48750, 4.62187, " +
//                    "0.00000, 0.37813, 1.51250, 3.40313, 4.55000, 3.82813, 3.86250, 4.65312, 4.70000, 4.94062]",
//
//            values.toString()
//        )

        assertEquals(
            "[0.00000, 0.50000, 1.00000, 1.50000, 2.00000, 2.50000, 3.00000, 3.50000, 4.00000, 4.50000, " +
                    "0.00000, 0.05000, 0.20000, 0.45000, 0.80000, 1.25000, 1.80000, 2.45000, 3.20000, 4.05000, " +
                    "0.00000, 0.95000, 1.80000, 2.55000, 3.20000, 3.75000, 4.20000, 4.55000, 4.80000, 4.95000, " +
                    "0.00000, 0.10000, 0.40000, 0.90000, 1.60000, 2.50000, 3.40000, 4.10000, 4.60000, 4.90000, " +
                    "0.00000, 0.00500, 0.04000, 0.13500, 0.32000, 0.62500, 1.08000, 1.71500, 2.56000, 3.64500, " +
                    "0.00000, 1.35500, 2.44000, 3.28500, 3.92000, 4.37500, 4.68000, 4.86500, 4.96000, 4.99500, " +
                    "0.00000, 0.02000, 0.16000, 0.54000, 1.28000, 2.50000, 3.72000, 4.46000, 4.84000, 4.98000, " +
                    "0.00000, 5.0E-40, 0.00800, 0.04050, 0.12800, 0.31250, 0.64800, 1.20050, 2.04800, 3.28050, " +
                    "0.00000, 1.71950, 2.95200, 3.79950, 4.35200, 4.68750, 4.87200, 4.95950, 4.99200, 4.99950, " +
                    "0.00000, 0.00400, 0.06400, 0.32400, 1.02400, 2.50000, 3.97600, 4.67600, 4.93600, 4.99600, " +
                    "0.00000, 5.0E-50, 0.00160, 0.01215, 0.05120, 0.15625, 0.38880, 0.84035, 1.63840, 2.95245, " +
                    "0.00000, 2.04755, 3.36160, 4.15965, 4.61120, 4.84375, 4.94880, 4.98785, 4.99840, 4.99995, " +
                    "0.00000, 8.0E-40, 0.02560, 0.19440, 0.81920, 2.50000, 4.18080, 4.80560, 4.97440, 4.99920, " +
                    "0.00488, 0.00977, 0.01953, 0.03906, 0.07813, 0.15625, 0.31250, 0.62500, 1.25000, 2.50000, " +
                    "0.00000, 2.50000, 3.75000, 4.37500, 4.68750, 4.84375, 4.92188, 4.96094, 4.98047, 4.99023, " +
                    "0.00244, 0.00977, 0.03906, 0.15625, 0.62500, 2.50000, 4.37500, 4.84375, 4.96094, 4.99023, " +
                    "0.00000, 0.06156, 0.24472, 0.54497, 0.95492, 1.46447, 2.06107, 2.73005, 3.45492, 4.21783, " +
                    "0.00000, 0.78217, 1.54509, 2.26995, 2.93893, 3.53553, 4.04509, 4.45503, 4.75528, 4.93844, " +
                    "0.00000, 0.12236, 0.47746, 1.03054, 1.72746, 2.50000, 3.27254, 3.96946, 4.52254, 4.87764, " +
                    "0.00000, 0.02506, 0.10102, 0.23030, 0.41742, 0.66987, 1.00000, 1.42929, 2.00000, 2.82055, " +
                    "0.00000, 2.17945, 3.00000, 3.57071, 4.00000, 4.33013, 4.58258, 4.76970, 4.89898, 4.97494, " +
                    "0.00000, 0.05051, 0.20871, 0.50000, 1.00000, 2.50000, 4.00000, 4.50000, 4.79129, 4.94949, " +
                    "0.00000, -0.07157, -0.23225, -0.40100, -0.49676, -0.43849, -0.14514, 0.46434, 1.47099, 2.95586, " +
                    "0.00000, 2.04414, 3.52901, 4.53566, 5.14514, 5.43849, 5.49676, 5.40100, 5.23225, 5.07157, " +
                    "0.00000, 0.05938, 0.30000, 0.34688, 1.13750, 1.17188, 0.45000, 1.59687, 3.48750, 4.62187, " +
                    "0.00000, 0.37813, 1.51250, 3.40313, 4.55000, 3.82813, 3.86250, 4.65312, 4.70000, 4.94062]",

            values.toString()
        )
    }

    private val input = floatArrayOf(
        0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f
    )


    @Test
    fun testLinear() {
        val output = input.map { Easing.EasingFunctions.linear(it) }
        kotlin.test.assertEquals(
            "[0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]",
            output.toString()
        )
    }

    @Test
    fun testPoly3In() {
        val easingFunction = Easing.EasingFunctions.polyIn()
        val output = input.map { StringUtils.formatFloat(easingFunction(it), 3) }
        kotlin.test.assertEquals(
            "[0.000, 0.001, 0.008, 0.027, 0.064, 0.125, 0.216, 0.343, 0.512, 0.729, 1.000]",
            output.toString()
        )
    }

    @Test
    fun testPoly25In() {
        val easingFunction = Easing.EasingFunctions.polyIn(2.5)
        val output = input.map { StringUtils.formatFloat(easingFunction(it), 6) }
        kotlin.test.assertEquals(
            "[0.000000, 0.003162, 0.017889, 0.049295, 0.101193, 0.176777, 0.278855, 0.409963, 0.572433, 0.768433, 1.000000]",
            output.toString()
        )
    }

    @Test
    fun testPoly3Out() {
        val outF = Easing.EasingFunctions.polyOut()
        val inF = Easing.EasingFunctions.polyIn()
        val output = input.map { StringUtils.formatFloat(outF(it), 3) }
        kotlin.test.assertEquals(
            "[0.000, 0.271, 0.488, 0.657, 0.784, 0.875, 0.936, 0.973, 0.992, 0.999, 1.000]",
            output.toString()
        )

        val divOut = input.map { StringUtils.formatFloat(outF(it) + inF(1 - it), 3) }
        kotlin.test.assertEquals(
            "[1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000]",
            divOut.toString()
        )
    }

    @Test
    fun testPoly3InOut() {
        val easingFunction = Easing.EasingFunctions.polyInOut()
        val output = input.map { StringUtils.formatFloat(easingFunction(it), 3) }
        kotlin.test.assertEquals(
            "[0.000, 0.004, 0.032, 0.108, 0.256, 0.500, 0.744, 0.892, 0.968, 0.996, 1.000]",
            output.toString()
        )
    }

}
