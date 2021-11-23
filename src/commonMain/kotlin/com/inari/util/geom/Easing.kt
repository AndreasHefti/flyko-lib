package com.inari.util.geom

import com.inari.util.geom.GeomUtils.halfPi
import com.inari.util.geom.GeomUtils.tau
import kotlin.jvm.JvmField
import kotlin.math.*

interface EasingFunction {
    operator fun invoke(t: Float): Float
}

object Easing {

    @JvmField val LINEAR = object : EasingFunction {
        override fun invoke(t: Float) = t
    }
    @JvmField val QUAD_IN = polyIn(2.0)
    @JvmField val QUAD_OUT = polyOut(2.0)
    @JvmField val QUAD_IN_OUT = polyInOut(2.0)
    @JvmField val CUBIC_IN = polyIn(3.0)
    @JvmField val CUBIC_OUT = polyOut(3.0)
    @JvmField val CUBIC_IN_OUT = polyInOut(3.0)
    @JvmField val QRT_IN = polyIn(4.0)
    @JvmField val QRT_OUT = polyOut(4.0)
    @JvmField val QRT_IN_OUT = polyInOut(4.0)
    @JvmField val QNT_IN = polyIn(5.0)
    @JvmField val QNT_OUT = polyOut(5.0)
    @JvmField val QNT_IN_OUT = polyInOut(5.0)
    @JvmField val EXPO_IN = object : EasingFunction {
        override fun invoke(t: Float) = 2.0.pow(10.0 * t - 10.0).toFloat()
    }
    @JvmField val EXPO_OUT = object : EasingFunction {
        override fun invoke(t: Float) = 1f - 2.0.pow(-10.0 * t).toFloat()
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
        override fun invoke(t: Float) = 1f - cos(t * halfPi).toFloat()
    }
    @JvmField val SIN_OUT = object : EasingFunction {
        override fun invoke(t: Float) = sin(t * halfPi).toFloat()
    }
    @JvmField val SIN_IN_OUT = object : EasingFunction {
        override fun invoke(t: Float) = (1f - cos(PI * t)).toFloat() / 2f
    }
    @JvmField val CIRC_IN = object : EasingFunction {
        override fun invoke(t: Float) = 1.0f - sqrt(1.0 - t * t).toFloat()
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
    @JvmField val BACK_IN = backIn()
    @JvmField val BACK_OUT = backOut()
    @JvmField val BONCE_IN = bonceIn()
    @JvmField val BONCE_OUT = bonceOut()


    inline fun polyIn(exp: Double): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float = t.toDouble().pow(exp).toFloat()
    }
    inline fun polyOut(exp: Double): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float = 1f - (1.0 - t.toDouble()).pow(exp).toFloat()
    }
    inline fun polyInOut(exp: Double): EasingFunction = object : EasingFunction {
        override fun invoke(t: Float): Float {
            val tt = t * 2f
            return if (tt <= 1f)
                (tt.toDouble().pow(exp)).toFloat() / 2f
            else
                (2f - (2.0 - tt).pow(exp)).toFloat() / 2f
        }
    }

    inline fun backIn(backFactor: Float = 1.70158f) = object : EasingFunction {
        override operator fun invoke(t: Float): Float {
            return t * t * ((backFactor + 1f) * t - backFactor)
        }
    }

    inline fun backOut(backFactor: Float = 1.70158f) = object : EasingFunction {
        override operator fun invoke(t: Float): Float {
            val tt = t - 1f
            return tt * tt * ((backFactor + 1f) * tt + backFactor) + 1f
        }
    }

    inline fun elasticIn(amplitude: Float = 1f, period: Float = 0.3f) = object : EasingFunction {
        val a = 1f.coerceAtLeast(amplitude)
        val p = period / tau
        val s = asin(1.0 / a) * p
        override operator fun invoke(t: Float): Float {
            val tt = t - 1f
            return (a * 2.0.pow(10.0 * tt) * sin((s - tt) / p)).toFloat()
        }
    }

    inline fun elasticOut(amplitude: Float = 1f, period: Float = 0.3f) = object : EasingFunction {
        val a = 1f.coerceAtLeast(amplitude)
        val p = period / tau
        val s = asin(1.0 / a) * p
        override operator fun invoke(t: Float): Float {
            val tt = t + 1f
            return  (1f - a * 2.0.pow(-10.0 * tt) * sin((tt + s) / p)).toFloat()
        }
    }

    inline fun elasticInOut(amplitude: Float = 1f, period: Float = 0.3f) = object : EasingFunction {
        val a = 1f.coerceAtLeast(amplitude)
        val p = period / tau
        val s = asin(1.0 / a) * p
        override operator fun invoke(t: Float): Float {
            val tt = t * 2f - 1f
            return if (tt < 0)
                (a * 2.0.pow(10.0 * tt) * sin((s - tt) / p)).toFloat()
            else
                ((2f - a * 2.0.pow(-10.0 * tt) * sin((s + tt) / p)) / 2f).toFloat()
        }
    }

    inline fun bonceIn(
        b1: Float = 4f / 11f,
        b2: Float = 6f / 11f,
        b3: Float = 8f / 11f,
        b4: Float = 3f / 4f,
        b5: Float = 9f / 11f,
        b6: Float = 10f / 11f,
        b7: Float = 15f / 16f,
        b8: Float = 21f / 22f,
        b9: Float = 63 / 64f
    ) = object : EasingFunction {
        val bounceOut = bonceOut(b1, b2, b3, b4, b5, b6, b7, b8, b9)
        override operator fun invoke(t: Float): Float =
            1f - bounceOut(1f - t)
    }


    inline fun bonceOut(
        b1: Float = 4f / 11f,
        b2: Float = 6f / 11f,
        b3: Float = 8f / 11f,
        b4: Float = 3f / 4f,
        b5: Float = 9f / 11f,
        b6: Float = 10f / 11f,
        b7: Float = 15f / 16f,
        b8: Float = 21f / 22f,
        b9: Float = 63 / 64f
    ) = object : EasingFunction {
        val b0 = 1f / b1 / b1
        override operator fun invoke(t: Float): Float {
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

//        fun bonceInOut(
//            b1: Float = 4f / 11f,
//            b2: Float = 6f / 11f,
//            b3: Float = 8f / 11f,
//            b4: Float = 3f / 4f,
//            b5: Float = 9f / 11f,
//            b6: Float = 10f / 11f,
//            b7: Float = 15f / 16f,
//            b8: Float = 21f / 22f,
//            b9: Float = 63 / 64f
//        ) = object : EasingFunction {
//            val bounceOut = bonceOut(b1, b2, b3, b4, b5, b6, b7, b8, b9)
//            override operator fun invoke(t: Float): Float {
//                val tt = t * 2f
//                return if (tt < 1)
//                    (1f - bounceOut(1f - t)) / 2f
//                else
//                    (bounceOut(t - 1f) + 1f) / 2f
//            }
//        }

//        fun backInOut(backFactor: Float = 1.70158f) = object : EasingFunction {
//            override operator fun invoke(t: Float): Float {
//                val tt = t * 2f
//                return if (tt < 1f) {
//                    t * ((backFactor + 1f) * t - backFactor) / 2f
//                } else {
//                    val ttt = tt - 2f
//                    (ttt * ttt * ((backFactor + 1f) * ttt + backFactor) + 2f) / 2f
//                }
//            }
//        }

}