package com.inari.util

import kotlin.system.getTimeMillis


actual fun randomUUID(): String = throw UnsupportedOperationException()

actual fun timeMillis(): Long = getTimeMillis()

actual fun arraycopy(source: FloatArray, fromS: Int, dest: FloatArray, fromD: Int, size: Int) {
    source.copyInto(dest, fromD, fromS, size)
}
actual fun arraycopy(source: IntArray, fromS: Int, dest: IntArray, fromD: Int, size: Int) {
    source.copyInto(dest, fromD, fromS, size)
}
actual fun <T : Any?> arraycopy(source: Array<T>, fromS: Int, dest: Array<T>, fromD: Int, size: Int) {
    source.copyInto(dest, fromD, fromS, size)
}
