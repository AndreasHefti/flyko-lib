package com.inari.firefly.info

import com.inari.firefly.BlendMode
import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.SYSTEM_FONT
import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.core.api.ShapeData
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.core.system.FFSystem
import com.inari.firefly.graphics.text.FontAsset
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.util.Call
import com.inari.util.collection.DynArray
import com.inari.util.geom.Vector4f

object FFInfoSystem : FFSystem {

    private var active = false
    private val infos: DynArray<SysInfo> = DynArray.of()
    private val buffer = StringBuffer()

    private var width = 0
    private var hStep = 0
    private var vStep = 0

    private var font: FontAsset
    private val graphics = FFContext.graphics

    private val postRenderListener: Call = {
            graphics.startRendering( ViewSystem.baseView.data, false )
            renderSystemInfoDisplay()
            graphics.endRendering( ViewSystem.baseView.data )
            graphics.flush(FFApp.NO_VIRTUAL_VIEW_PORTS)
    }

    init {
        if (SYSTEM_FONT !in AssetSystem.assets)
            throw ExceptionInInitializerError("No FontAsset for SYSTEM_FONT found in AssetSystem. SYSTEM_FONT must be defined")
        font = AssetSystem.assets.getAs(SYSTEM_FONT)
        hStep = font.charWidth + font.charSpace
        vStep = font.charHeight + font.lineSpace
    }

    fun activate(): FFInfoSystem {
        if (active)
            return this

        FFContext.registerListener(FFApp.PostRenderEvent, postRenderListener)
        active = true
        return this
    }

    fun deactivate(): FFInfoSystem {
        if (!active)
            return this

        FFContext.disposeListener(FFApp.PostRenderEvent, postRenderListener)
        active = false
        return this
    }

    fun addInfo(info: SysInfo): FFInfoSystem {
        if (info in infos)
            return this

        infos.add(info)
        buffer.append( CharArray(info.length) )
        buffer.append( '\n' )
        if ( width < info.length ) {
            width = info.length
        }
        return this
    }

    private fun renderSystemInfoDisplay() {
        update()

        infoDisplayBackground.rectVertices[2] = (width * hStep + hStep).toFloat()
        infoDisplayBackground.rectVertices[3] = (infos.size * vStep + vStep).toFloat()
        graphics.renderShape(infoDisplayBackground.data, 0f, 0f)

        var xpos = 5f
        var ypos = 5f

        for (i in 0 until buffer.length) {
            val character = buffer[i]
            if (character == '\n') {
                xpos = 0f
                ypos += vStep
                continue
            }

            textRenderable.spriteId = font[character]
            graphics.renderSprite(textRenderable, xpos, ypos)
            xpos += hStep
        }
    }

    fun update() {
        var startIndex = 0
        var i = 0
        while (i < infos.capacity) {
            val info = infos[i++] ?: continue

            info.update(buffer, startIndex)
            startIndex += info.length + 1
        }
    }

    override fun clearSystem() {
        infos.clear()
    }

    interface SysInfo {
        val name: String
        val length: Int
        fun update(buffer: StringBuffer, bufferStartPointer: Int)
    }

    private val textRenderable = SpriteRenderable(blendMode = BlendMode.NORMAL_ALPHA)
    private val infoDisplayBackground = object {

        @JvmField val color = Vector4f(0.8f, 0.8f, 0.8f, 0.5f)
        @JvmField val rectVertices: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)
        @JvmField val data = ShapeData(
            type = ShapeType.RECTANGLE,
            vertices = rectVertices,
            color1 = color,
            color2 = color,
            color3 = color,
            color4 = color,
            blend = BlendMode.NORMAL_ALPHA,
            fill = true
        )
    }

}