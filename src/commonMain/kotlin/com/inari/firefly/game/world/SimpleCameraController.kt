package com.inari.firefly.game.world

import com.inari.firefly.FFContext
import com.inari.firefly.control.Controller
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewChangeEvent
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField
import kotlin.math.ceil
import kotlin.math.floor


class SimpleCameraController private constructor() : Controller() {

    @JvmField var pixelPerfect = false
    @JvmField var pivot: Vector2f = Vector2f()
    @JvmField var snapToBounds: Vector4i = Vector4i()
    @JvmField var velocity: Float = 0.25f

    private val pos = Vector2f()
    private lateinit var view: View
    private lateinit var viewChangeEvent: ViewChangeEvent

    fun adjust() {
        if (getPos(view.data.zoom, view.data.bounds, view.data.worldPosition)) {
            view.data.worldPosition.x = floor(view.data.worldPosition.x + pos.x)
            view.data.worldPosition.y = floor(view.data.worldPosition.y + pos.y)
            FFContext.notify(viewChangeEvent)
        }
    }

    override fun init(componentId: CompId) {
        this.view = FFContext[componentId]
        viewChangeEvent = ViewChangeEvent.of(view.componentId, ViewChangeEvent.Type.ORIENTATION, pixelPerfect)
    }

    override fun update(componentId: CompId) {
        if (getPos(view.data.zoom, view.data.bounds, view.data.worldPosition)) {
            view.data.worldPosition.x += pos.x * velocity
            view.data.worldPosition.y += pos.y * velocity
            if (pixelPerfect) {
                view.data.worldPosition.x = floor(view.data.worldPosition.x)
                view.data.worldPosition.y = floor(view.data.worldPosition.y)
            }
            FFContext.notify(viewChangeEvent)
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

    companion object : SystemComponentSubType<Controller, SimpleCameraController>(Controller, SimpleCameraController::class) {
        override fun createEmpty() = SimpleCameraController()
    }
}