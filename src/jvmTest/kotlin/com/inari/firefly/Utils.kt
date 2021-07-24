package com.inari.firefly

fun measureTime(message: String, times: Int = 1, expr: () -> Unit)  {
    val time = System.currentTimeMillis()
    var i = 0
    while (i < times) {
        expr()
        i++
    }
    println("${System.currentTimeMillis() - time} : $message")
}