package com.inari.firefly.misc

object PropsTest3 {

    val prop: Prop = Prop()

    class Prop {
        init {
            println("Prop created")
        }
    }
}