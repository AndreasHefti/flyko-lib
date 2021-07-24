package com.inari.firefly.info

import com.inari.firefly.FFContext

object FrameRateInfo : FFInfoSystem.SysInfo{

    private var lastSecondTime = -1L
    private var frames = 0

    private val info = "FPS:........00".toCharArray()

    override val name: String = "FrameRateInfo"
    override val length: Int = info.size

    override fun update(buffer: StringBuffer, bufferStartPointer: Int) {
        val timer = FFContext.timer
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

    private fun updateFPS(buffer: StringBuffer, bufferStartPointer: Int, fps: String) =
        buffer.replace(bufferStartPointer + (info.size - fps.length), bufferStartPointer + info.size, fps)


    private fun setText(buffer: StringBuffer, bufferStartPointer: Int) {
        for (i in 0 until info.size) {
            buffer.setCharAt(i + bufferStartPointer, info[i])
        }
    }

}