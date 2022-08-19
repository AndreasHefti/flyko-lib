package com.inari.firefly.filter

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.TextureData
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.GdxRuntimeException

abstract class FilteredTextureData @JvmOverloads constructor(
    private val fileHandle: FileHandle,
    preloadedPixmap: Pixmap = Pixmap(fileHandle),
    format: Format? = null
) : TextureData {

    private var width = 0
    private var height = 0
    private var format: Format? = null
    private var pixmap: Pixmap? = null
    private var useMipMaps = true
    private var isPrepared = false

    constructor(resourcePath: String) : this(Gdx.files.internal(resourcePath))

    init {
        this.pixmap = preloadedPixmap
        this.format = format

        if (pixmap != null) {
            pixmap = ensurePot(pixmap!!)
            width = pixmap!!.width
            height = pixmap!!.height
            if (format == null) this.format = pixmap!!.format
        }
    }

    override fun isPrepared(): Boolean {
        return isPrepared
    }

    override fun prepare() {
        if (isPrepared)
            throw GdxRuntimeException("Already prepared")

        if (pixmap == null) {
            pixmap = if (fileHandle.extension() == "cim")
                PixmapIO.readCIM(fileHandle)
            else
                ensurePot(Pixmap(fileHandle))

            width = pixmap!!.width
            height = pixmap!!.height
            if (format == null)
                format = pixmap!!.format
        }

        applyFilter(pixmap!!)
        isPrepared = true
    }

    private fun ensurePot(pixmap: Pixmap): Pixmap {
        if (Gdx.gl20 == null && copyToPOT) {
            val pixmapWidth = pixmap.width
            val pixmapHeight = pixmap.height
            val potWidth = MathUtils.nextPowerOfTwo(pixmapWidth)
            val potHeight = MathUtils.nextPowerOfTwo(pixmapHeight)
            if (pixmapWidth != potWidth || pixmapHeight != potHeight) {
                val tmp = Pixmap(potWidth, potHeight, pixmap.format)
                tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmapWidth, pixmapHeight)
                pixmap.dispose()
                return tmp
            }
        }
        return pixmap
    }

    override fun consumePixmap(): Pixmap {
        if (!isPrepared) throw GdxRuntimeException("Call prepare() before calling getPixmap()")
        isPrepared = false
        val pixmap = this.pixmap
        this.pixmap = null
        return pixmap!!
    }

    override fun disposePixmap(): Boolean {
        return true
    }

    override fun getWidth(): Int = width
    override fun getHeight(): Int = height
    override fun getFormat(): Format? = format
    override fun useMipMaps(): Boolean = useMipMaps
    override fun isManaged(): Boolean = true
    override fun getType(): TextureData.TextureDataType = TextureData.TextureDataType.Pixmap
    override fun consumeCustomData(target: Int) = throw GdxRuntimeException("This TextureData implementation does not upload data itself")

    protected abstract fun applyFilter(pixmap: Pixmap)

    companion object {
        var copyToPOT: Boolean = false
    }
}
