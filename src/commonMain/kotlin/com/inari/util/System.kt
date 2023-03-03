package com.inari.util

expect fun randomUUID(): String

expect fun timeMillis(): Long

expect fun arraycopy(source: FloatArray, fromS: Int, dest: FloatArray, fromD: Int, size: Int)
expect fun arraycopy(source: IntArray, fromS: Int, dest: IntArray, fromD: Int, size: Int)
expect fun <T : Any?> arraycopy(source: Array<T>, fromS: Int, dest: Array<T>, fromD: Int, size: Int)

expect val currentThreadName: String
expect fun startParallelTask(name: String, task: () -> Unit, callback: (Error?) -> Unit)
expect fun sleepCurrentThread(milliseconds: Long)
