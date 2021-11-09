package com.inari.firefly.graphics.text

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.graphics.effect.ShaderAsset
import com.inari.firefly.graphics.rendering.Renderer
import com.inari.firefly.graphics.rendering.SimpleTextRenderer
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

class EText private constructor() : EntityComponent(EText::class.simpleName!!) {

    @JvmField internal var rendererRef = SimpleTextRenderer.instance.index
    @JvmField internal var fontAssetRef = -1

    val renderer = ComponentRefResolver(Renderer) { index-> rendererRef = index }
    var fontAsset = ComponentRefResolver(Asset) { index -> fontAssetRef = index }

    val text: StringBuilder = StringBuilder()
    var tint: MutableColor = MutableColor(1f, 1f, 1f, 1f)
    var blend: BlendMode = BlendMode.NONE

    override fun reset() {
        rendererRef = -1
        fontAssetRef = -1
        text.setLength(0)
        tint.r_mutable = 1f; tint.g_mutable = 1f; tint.b_mutable = 1f; tint.a_mutable = 1f
        blend = BlendMode.NONE
    }

    override fun componentType(): ComponentType<EText> = Companion
    companion object : EntityComponentType<EText>(EText::class) {
        override fun createEmpty() = EText()
    }
}