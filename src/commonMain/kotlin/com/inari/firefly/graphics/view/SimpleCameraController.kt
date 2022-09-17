package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.graphics.view.View.ViewChangeEvent.Type.*
import com.inari.firefly.graphics.view.View.ViewChangeEvent
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField
import kotlin.math.ceil
import kotlin.math.floor

class SimpleCameraController private constructor() : SystemControl(View) {

    @JvmField var pixelPerfect = false
    @JvmField var pivot: Vector2f = Vector2f()
    @JvmField var snapToBounds: Vector4i = Vector4i()
    @JvmField var velocity: Float = 0.25f

    private val pos = Vector2f()
    private lateinit var view: View
    private lateinit var viewChangeEvent: ViewChangeEvent

    fun adjust() {
        if (getPos(view.zoom, view.bounds, view.worldPosition)) {
            view.worldPosition.x = floor(view.worldPosition.x + pos.x)
            view.worldPosition.y = floor(view.worldPosition.y + pos.y)
            Engine.notify(viewChangeEvent)
        }
    }

    override fun update(index: Int) {}
    override fun matchForControl(key: ComponentKey): Boolean {
        val comp = View[key]
        if (this.index in comp.controllerReferences) {
            this.view = View[key]
            viewChangeEvent = View.createViewChangeEvent(view.index, ORIENTATION, pixelPerfect)
            return true
        }
        return false
    }

    override fun update() {
        if (getPos(view.zoom, view.bounds, view.worldPosition)) {
            view.worldPosition.x += pos.x * velocity
            view.worldPosition.y += pos.y * velocity
            if (pixelPerfect) {
                view.worldPosition.x = floor(view.worldPosition.x)
                view.worldPosition.y = floor(view.worldPosition.y)
            }
            Engine.notify(viewChangeEvent)
        }
    }

    private fun getPos(zoom: Float, viewBounds: Vector4i, worldPosition: Vector2f): Boolean {

        val following = pivot
        val oneDivZoom = 1f / zoom
        val viewHorizontal = viewBounds.width / oneDivZoom
        val viewHorizontalHalf = viewHorizontal / 2f
        val viewVertical = viewBounds.height / oneDivZoom
        val viewVerticalHalf = viewVertical / 2f

        val xMax = snapToBounds.width - viewHorizontal
        val yMax = snapToBounds.height - viewVertical

        pos.x = following.x + oneDivZoom - viewHorizontalHalf
        pos.y = following.y + oneDivZoom - viewVerticalHalf

        if (pos.x < snapToBounds.x)
            pos.x = snapToBounds.x.toFloat()
        if (pos.y < snapToBounds.y)
            pos.y = snapToBounds.y.toFloat()

        pos.x = pos.x.coerceAtMost(xMax)
        pos.y = pos.y.coerceAtMost(yMax)
        pos.x = ceil(pos.x - worldPosition.x)
        pos.y = floor(pos.y - worldPosition.y)

        return pos.x != 0f || pos.y != 0f
    }

    companion object : ComponentSubTypeBuilder<Control, SimpleCameraController>(Control, "SimpleCameraController") {
        override fun create() = SimpleCameraController()
    }
}