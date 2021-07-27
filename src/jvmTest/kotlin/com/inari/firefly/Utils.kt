package com.inari.firefly

import com.inari.util.timeMillis

fun measureTime(message: String, times: Int = 1, expr: () -> Unit)  {
    val time = timeMillis()
    var i = 0
    while (i < times) {
        expr()
        i++
    }
    println("${timeMillis() - time} : $message")
}