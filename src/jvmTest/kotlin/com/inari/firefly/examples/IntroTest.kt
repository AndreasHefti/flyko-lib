package com.inari.firefly.examples

import com.inari.firefly.DesktopRunner
import com.inari.firefly.core.api.DesktopAppAdapter

fun main(args: Array<String>) {
    object : DesktopRunner("IntroTest", 704, 480) {
        override fun init() {
            dispose()
            DesktopAppAdapter.exit()
        }
    }
}

