package com.inari.util.geom

import kotlin.jvm.JvmField
import kotlin.math.*


/** Interface to calculate a specified subType of Easing  */
interface Easing {

    /** Defines a Easing repository with different types of Easing implementations  */
    enum class Type constructor(val func: EasingFunctions.EasingFunction) : EasingFunctions.EasingFunction {
        LINEAR(EasingFunctions.linear),
        QUAD_IN(EasingFunctions.polyIn(2.0)),
        QUAD_OUT(EasingFunctions.polyOut(2.0)),
        QUAD_IN_OUT(EasingFunctions.polyInOut(2.0)),
        CUBIC_IN(EasingFunctions.polyIn()),
        CUBIC_OUT(EasingFunctions.polyOut()),
        CUBIC_IN_OUT(EasingFunctions.polyInOut()),
        QRT_IN(EasingFunctions.polyIn(4.0)),
        QRT_OUT(EasingFunctions.polyOut(4.0)),
        QRT_IN_OUT(EasingFunctions.polyInOut(4.0)),
        QNT_IN(EasingFunctions.polyIn(5.0)),
        QNT_OUT(EasingFunctions.polyOut(5.0)),
        QNT_IN_OUT(EasingFunctions.polyInOut(5.0)),
        EXPO_IN(EasingFunctions.expIn),
        EXPO_OUT(EasingFunctions.expOut),
        EXPO_IN_OUT(EasingFunctions.expInOut),
        SIN_IN(EasingFunctions.sinIn),
        SIN_OUT(EasingFunctions.sinOut),
        SIN_IN_OUT(EasingFunctions.sinInOut),
        CIRC_IN(EasingFunctions.circleIn),
        CIRC_OUT(EasingFunctions.circleOut),
        CIRC_IN_OUT(EasingFunctions.circleInOut),
        BACK_IN(EasingFunctions.backIn()),
        BACK_OUT(EasingFunctions.backOut()),
        BONCE_IN(EasingFunctions.bonceIn()),
        BONCE_OUT(EasingFunctions.bonceOut())
        ;

        override operator fun invoke(t: Float): Float = func(t)
    }

    object EasingFunctions {

        const val halfPi = PI / 2.0
        const val tau = 2.0 * PI

        @JvmField val linear = object : EasingFunction {
            override operator fun invoke(t: Float): Float = t
        }

        fun polyIn(exp: Double = 3.0) = object : EasingFunction {
            override operator fun invoke(t: Float): Float = t.toDouble().pow(exp).toFloat()
        }

        fun polyOut(exp: Double = 3.0) = object : EasingFunction {
            override operator fun invoke(t: Float): Float = 1f - (1.0 - t.toDouble()).pow(exp).toFloat()
        }

        fun polyInOut(exp: Double = 3.0) = object : EasingFunction {
            override operator fun invoke(t: Float): Float {
                val tt = t * 2f
                return (if (tt <= 1f)
                    tt.toDouble().pow(exp)
                else
                    2f - (2.0 - tt).pow(exp)
                    ).toFloat() / 2f
            }
        }

        @JvmField val sinIn = object : EasingFunction {
            override operator fun invoke(t: Float): Float = 1f - cos(t * halfPi).toFloat()
        }

        @JvmField val sinOut = object : EasingFunction {
            override operator fun invoke(t: Float): Float = sin(t * halfPi).toFloat()
        }

        @JvmField val sinInOut = object : EasingFunction {
            override operator fun invoke(t: Float): Float = (1f - cos(PI * t)).toFloat() / 2f
        }

        @JvmField val expIn = object : EasingFunction {
            override operator fun invoke(t: Float): Float = 2.0.pow(10.0 * t - 10.0).toFloat()
        }

        @JvmField val expOut = object : EasingFunction {
            override operator fun invoke(t: Float): Float = 1f - 2.0.pow(-10.0 * t).toFloat()
        }

        @JvmField val expInOut = object : EasingFunction {
            override operator fun invoke(t: Float): Float {
                val tt = t * 2f
                return if (tt <= 1.0f)
                    2.0.pow(10.0 * tt - 10).toFloat() / 2f
                else
                    (2f - 2.0.pow(10.0 - 10.0 * tt)).toFloat() / 2f
            }
        }

        @JvmField val circleIn = object : EasingFunction {
            override operator fun invoke(t: Float): Float = 1.0f - sqrt(1.0 - t * t).toFloat()
        }

        @JvmField val circleOut = object : EasingFunction {
            override operator fun invoke(t: Float): Float {
                val tt = t - 1f
                return sqrt(1.0 - tt * tt).toFloat()
            }
        }

        @JvmField val circleInOut = object : EasingFunction {
            override operator fun invoke(t: Float): Float {
                val tt = t * 2f
                return if (tt <= 1.0f) {
                    (1f - sqrt(1.0 - tt * tt)).toFloat() / 2f
                } else {
                    val ttt = tt - 2
                    (sqrt(1.0 - ttt * ttt) + 1f).toFloat() / 2f
                }
            }
        }

        fun elasticIn(amplitude: Float = 1f, period: Float = 0.3f) = object : EasingFunction {
            val a = 1f.coerceAtLeast(amplitude)
            val p = period / tau
            val s = asin(1.0 / a) * p
            override operator fun invoke(t: Float): Float {
                val tt = t - 1f
                return (a * 2.0.pow(10.0 * tt) * sin((s - tt) / p)).toFloat()
            }
        }

        fun elasticOut(amplitude: Float = 1f, period: Float = 0.3f) = object : EasingFunction {
            val a = 1f.coerceAtLeast(amplitude)
            val p = period / tau
            val s = asin(1.0 / a) * p
            override operator fun invoke(t: Float): Float {
                val tt = t + 1f
                return  (1f - a * 2.0.pow(-10.0 * tt) * sin((tt + s) / p)).toFloat()
            }
        }

        fun elasticInOut(amplitude: Float = 1f, period: Float = 0.3f) = object : EasingFunction {
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

        fun backIn(backFactor: Float = 1.70158f) = object : EasingFunction {
            override operator fun invoke(t: Float): Float {
                return t * t * ((backFactor + 1f) * t - backFactor)
            }

        }

        fun backOut(backFactor: Float = 1.70158f) = object : EasingFunction {
            override operator fun invoke(t: Float): Float {
                val tt = t - 1f
                return tt * tt * ((backFactor + 1f) * tt + backFactor) + 1f
            }

        }

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
        ) = object : EasingFunction {
            val bounceOut = bonceOut(b1, b2, b3, b4, b5, b6, b7, b8, b9)
            override operator fun invoke(t: Float): Float =
                1f - bounceOut(1f - t)
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


        interface EasingFunction {

            /** EasingFunctions calculation as function of t in the interval of 0 to 1
             * @param t current time in the interval 0 to 1
             * @return the easing value for current time
             */
            operator fun invoke(t: Float): Float
        }
    }
}

