package com.inari.util.geom

import com.inari.util.geom.GeomUtils.halfPi
import com.inari.util.geom.GeomUtils.tau
import kotlin.jvm.JvmField
import kotlin.math.*

// TODO make interface
// Note: It appears that Kotlin always use boxing for primitives in lambdas since it uses generic Function1...
//       We probed that with a profiler and ensured that with an interface it works as expected (no boxing of primitives)
//       This is a little bad for implementation and readability but way better for performance and minimizing GC runs
//typealias EasingFunction = (Float) -> Float
interface EasingFunction {
    operator fun invoke(t: Float): Float
}

object Easing {

    @JvmField val LINEAR = object : EasingFunction {
        override fun invoke(t: Float) = t
    }

    @JvmField val QUAD_IN = object : EasingFunction {
        private val pIn = polyIn(2.0)
        override fun invoke(t: Float): Float = pIn(t)
    }
    @JvmField val QUAD_OUT = object : EasingFunction {
        private val pOut = polyOut(2.0)
        override fun invoke(t: Float): Float = pOut(t)
    }
    @JvmField val QUAD_IN_OUT = object : EasingFunction {
        private val pInOut = polyInOut(2.0)
        override fun invoke(t: Float): Float = pInOut(t)
    }
    @JvmField val CUBIC_IN = object : EasingFunction {
        private val pIn = polyIn(3.0)
        override fun invoke(t: Float): Float = pIn(t)
    }
    @JvmField val CUBIC_OUT = object : EasingFunction {
        private val pOut = polyOut(3.0)
        override fun invoke(t: Float): Float = pOut(t)
    }
    @JvmField val CUBIC_IN_OUT = object : EasingFunction {
        private val pInOut = polyInOut(3.0)
        override fun invoke(t: Float): Float = pInOut(t)
    }
    @JvmField val QRT_IN = object : EasingFunction {
        private val pIn = polyIn(4.0)
        override fun invoke(t: Float): Float = pIn(t)
    }
    @JvmField val QRT_OUT = object : EasingFunction {
        private val pOut = polyOut(4.0)
        override fun invoke(t: Float): Float = pOut(t)
    }
    @JvmField val QRT_IN_OUT = object : EasingFunction {
        private val pInOut = polyInOut(4.0)
        override fun invoke(t: Float): Float = pInOut(t)
    }
    @JvmField val QNT_IN = object : EasingFunction {
        private val pIn = polyIn(5.0)
        override fun invoke(t: Float): Float = pIn(t)
    }
    @JvmField val QNT_OUT = object : EasingFunction {
        private val pOut = polyOut(5.0)
        override fun invoke(t: Float): Float = pOut(t)
    }
    @JvmField val QNT_IN_OUT = object : EasingFunction {
        private val pInOut = polyInOut(5.0)
        override fun invoke(t: Float): Float = pInOut(t)
    }
    @JvmField val EXPO_IN = object : EasingFunction {
        override fun invoke(t: Float): Float = 2.0.pow(10.0 * t - 10.0).toFloat()
    }
    @JvmField val EXPO_OUT = object : EasingFunction {
        override fun invoke(t: Float): Float = 1f - 2.0.pow(-10.0 * t).toFloat()
    }
    @JvmField val EXPO_IN_OUT = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val tt = t * 2f
            return if (tt <= 1.0f)
                2.0.pow(10.0 * tt - 10).toFloat() / 2f
            else
                (2f - 2.0.pow(10.0 - 10.0 * tt)).toFloat() / 2f
        }
    }
    @JvmField val SIN_IN = object : EasingFunction {
        override fun invoke(t: Float): Float = 1f - cos(t * halfPi).toFloat()
    }
    @JvmField val SIN_OUT = object : EasingFunction {
        override fun invoke(t: Float): Float = sin(t * halfPi).toFloat()
    }
    @JvmField val SIN_IN_OUT = object : EasingFunction {
        override fun invoke(t: Float): Float = (1f - cos(PI * t)).toFloat() / 2f
    }
    @JvmField val CIRC_IN = object : EasingFunction {
        override fun invoke(t: Float): Float = 1.0f - sqrt(1.0 - t * t).toFloat()
    }
    @JvmField val CIRC_OUT = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val tt = t - 1f
            return sqrt(1.0 - tt * tt).toFloat()
        }
    }
    @JvmField val CIRC_IN_OUT = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val tt = t * 2f
            return if (tt <= 1.0f) {
                (1f - sqrt(1.0 - tt * tt)).toFloat() / 2f
            } else {
                val ttt = tt - 2
                (sqrt(1.0 - ttt * ttt) + 1f).toFloat() / 2f
            }
        }
    }
    @JvmField val BACK_IN = object : EasingFunction {
        private val bIn = backIn()
        override fun invoke(t: Float): Float = bIn(t)
    }
    @JvmField val BACK_OUT = object : EasingFunction {
        private val bOut = backOut()
        override fun invoke(t: Float): Float = bOut(t)
    }
    @JvmField val BONCE_IN = object : EasingFunction {
        private val bIn =  bonceIn()
        override fun invoke(t: Float): Float = bIn(t)
    }
    @JvmField val BONCE_OUT = object : EasingFunction {
        private val bOut = bonceOut()
        override fun invoke(t: Float): Float = bOut(t)
    }

    fun polyIn(exp: Double): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float = t.toDouble().pow(exp).toFloat()
    }

    fun polyOut(exp: Double): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float = 1f - (1.0 - t.toDouble()).pow(exp).toFloat()
    }
    fun polyInOut(exp: Double): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val tt = t * 2f
            return if (tt <= 1f)
                (tt.toDouble().pow(exp)).toFloat() / 2f
            else
                (2f - (2.0 - tt).pow(exp)).toFloat() / 2f
        }
    }

    fun backIn(backFactor: Float = 1.70158f): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float = t * t * ((backFactor + 1f) * t - backFactor)
    }
    fun backOut(backFactor: Float = 1.70158f): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val tt = t - 1f
            return tt * tt * ((backFactor + 1f) * tt + backFactor) + 1f
        }
    }

    fun elasticIn(amplitude: Float = 1f, period: Float = 0.3f) : EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val a = 1f.coerceAtLeast(amplitude)
            val p = period / tau
            val s = asin(1.0 / a) * p
            val tt = t - 1f
            return (a * 2.0.pow(10.0 * tt) * sin((s - tt) / p)).toFloat()
        }
    }

    fun elasticOut(amplitude: Float = 1f, period: Float = 0.3f): EasingFunction =object : EasingFunction {
        override fun invoke(t: Float): Float {
            val a = 1f.coerceAtLeast(amplitude)
            val p = period / tau
            val s = asin(1.0 / a) * p
            val tt = t + 1f
            return (1f - a * 2.0.pow(-10.0 * tt) * sin((tt + s) / p)).toFloat()
        }
    }

    fun elasticInOut(amplitude: Float = 1f, period: Float = 0.3f): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val a = 1f.coerceAtLeast(amplitude)
            val p = period / tau
            val s = asin(1.0 / a) * p
            val tt = t * 2f - 1f
            return if (tt < 0)
                (a * 2.0.pow(10.0 * tt) * sin((s - tt) / p)).toFloat()
            else
                ((2f - a * 2.0.pow(-10.0 * tt) * sin((s + tt) / p)) / 2f).toFloat()
        }
    }

    fun bonceIn(
        b1: Float = 4f / 11f,
        b2: Float = 6f / 11f,
        b3: Float = 8f / 11f,
        b4: Float = 3f / 4f,
        b5: Float = 9f / 11f,
        b6: Float = 10f / 11f,
        b7: Float = 15f / 16f,
        b8: Float = 21f / 22f,
        b9: Float = 63 / 64f
    ): EasingFunction =  object : EasingFunction {
        val bounceOut = bonceOut(b1, b2, b3, b4, b5, b6, b7, b8, b9)
        override fun invoke(t: Float): Float = 1f - bounceOut(1f - t)
    }

    fun bonceOut(
        b1: Float = 4f / 11f,
        b2: Float = 6f / 11f,
        b3: Float = 8f / 11f,
        b4: Float = 3f / 4f,
        b5: Float = 9f / 11f,
        b6: Float = 10f / 11f,
        b7: Float = 15f / 16f,
        b8: Float = 21f / 22f,
        b9: Float = 63 / 64f
    ): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val b0 = 1f / b1 / b1
            return when {
                t < b1 -> b0 * t * t
                t < b3 -> {
                    val tt = t - b2
                    b0 * tt * tt + b4
                }
                t < b6 -> {
                    val tt = t - b5
                    b0 * tt * tt + b7
                }
                else -> {
                    val tt = t - b8
                    b0 * tt * tt + b9
                }
            }
        }
    }
}