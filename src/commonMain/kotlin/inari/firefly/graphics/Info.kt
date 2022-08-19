package com.inari.firefly.graphics

import com.inari.firefly.core.Asset
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Engine.Companion.SYSTEM_FONT
import com.inari.firefly.core.PostRenderEvent
import com.inari.firefly.core.System
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ShapeData
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.graphics.text.Font
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewSystemRenderer.NO_VIRTUAL_VIEW_PORTS
import com.inari.util.collection.DynArray
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

object FFInfoSystem : System {

    private var active = false
    private val infos: DynArray<SysInfo> = DynArray.of()
    private val buffer = StringBuilder()

    private var width = 0
    private var hStep = 0
    private var vStep = 0

    private var font: Font
    private val graphics = Engine.graphics

    private val postRenderListener: () -> Unit = {
        graphics.startViewportRendering( View[View.BASE_VIEW_KEY], false )
        renderSystemInfoDisplay()
        graphics.endViewportRendering( View[View.BASE_VIEW_KEY] )
        graphics.flush(NO_VIRTUAL_VIEW_PORTS)
    }

    init {
        if (!Asset.exists(SYSTEM_FONT))
            throw IllegalStateException("No FontAsset for SYSTEM_FONT found in AssetSystem. SYSTEM_FONT must be defined")
        font = Asset[SYSTEM_FONT, Font]
        hStep = font.charWidth + font.charSpace
        vStep = font.charHeight + font.lineSpace
    }

    fun activate(): FFInfoSystem {
        if (active)
            return this

        Engine.registerListener(PostRenderEvent.postRenderEvent, postRenderListener)
        active = true
        return this
    }

    fun deactivate(): FFInfoSystem {
        if (!active)
            return this

        Engine.disposeListener(PostRenderEvent.postRenderEvent, postRenderListener)
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

            textRenderable.spriteIndex = font[character]
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
        fun update(buffer: StringBuilder, bufferStartPointer: Int)
    }

    private val textRenderable = object : SpriteRenderable {
        override var spriteIndex = -1
        override val tintColor = Vector4f()
        override val blendMode = BlendMode.NORMAL_ALPHA
    }

    private val infoDisplayBackground = object {

        @JvmField val color = Vector4f(0.8f, 0.8f, 0.8f, 0.5f)
        @JvmField val rectVertices: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)
        @JvmField val data = object : ShapeData {
            override val type = ShapeType.RECTANGLE
            override val vertices = rectVertices
            override val segments = 0
            override val color1 = color
            override val color2 = color
            override val color3 = color
            override val color4 = color
            override val blend = BlendMode.NORMAL_ALPHA
            override val fill = true

        }
    }
}

object FrameRateInfo : FFInfoSystem.SysInfo {

    private var lastSecondTime = -1L
    private var frames = 0

    private val info = "FPS:000".toCharArray()

    override val name: String = "FrameRateInfo"
    override val length: Int = info.size

    override fun update(buffer: StringBuilder, bufferStartPointer: Int) {
        val timer = Engine.timer
        if (lastSecondTime < 0) {
            lastSecondTime = timer.time
            setText(buffer, bufferStartPointer)
            frames++
            return
        }

        frames++
        val duration = timer.time - lastSecondTime
        if (duration > 1000) {
            updateFPS(buffer, bufferStartPointer, frames.toString())
            frames = 0
            lastSecondTime = timer.time
        }
    }

    private fun updateFPS(buffer: StringBuilder, bufferStartPointer: Int, fps: String) =
        buffer.replaceRange(bufferStartPointer + (info.size - fps.length), bufferStartPointer + info.size, fps)


    private fun setText(buffer: StringBuilder, bufferStartPointer: Int) {
        for (i in info.indices)
            buffer[i + bufferStartPointer] = info[i]
    }

}