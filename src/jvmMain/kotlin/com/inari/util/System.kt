package com.inari.util

import java.util.*

actual fun randomUUID(): String = UUID.randomUUID().toString()

actual fun timeMillis(): Long = System.currentTimeMillis()

actual fun arraycopy(source: FloatArray, fromS: Int, dest: FloatArray, fromD: Int, size: Int) =
    System.arraycopy(source, fromS, dest, fromD, size)
actual fun arraycopy(source: IntArray, fromS: Int, dest: IntArray, fromD: Int, size: Int)=
    System.arraycopy(source, fromS, dest, fromD, size)
actual fun <T : Any?> arraycopy(source: Array<T>, fromS: Int, dest: Array<T>, fromD: Int, size: Int)=
    System.arraycopy(source, fromS, dest, fromD, size)

actual fun startParallelTask(name: String, task: () -> Unit, callback: (String, Boolean) -> Unit) {
    Thread {
        try {
            task()
            callback(name, true)
        } catch (e: Exception) {
            println("Failed to run parallel task")
            e.printStackTrace()
            callback(name, false)
        }
    }.start()
}
