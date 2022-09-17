package com.inari.firefly.graphics.text

import com.inari.firefly.core.CReference
import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class EText private constructor() : EntityComponent(EText) {

    @JvmField var renderer: EntityRenderer = SimpleTextRenderer
    @JvmField var fontRef = CReference(Font)

    @JvmField val text: StringBuilder = StringBuilder()
    @JvmField var tint: Vector4f = Vector4f(1f, 1f, 1f, 1f)
    @JvmField var blend: BlendMode = BlendMode.NONE


    override fun reset() {
        renderer = SimpleTextRenderer
        fontRef.reset()
        text.setLength(0)
        tint.r = 1f; tint.g = 1f; tint.b = 1f; tint.a = 1f
        blend = BlendMode.NONE
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EText>("EText") {
        override fun create() = EText()
    }
}