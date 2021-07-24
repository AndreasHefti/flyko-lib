package com.inari.firefly.graphics.view.camera

import com.inari.util.geom.PositionF
import com.inari.firefly.NO_CAMERA_PIVOT
import com.inari.firefly.control.Controller
import com.inari.firefly.control.SingleComponentController
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewChangeEvent
import com.inari.util.geom.Rectangle
import kotlin.math.ceil
import kotlin.math.floor


class SimpleCameraController private constructor() : SingleComponentController() {

    private val pos = PositionF()
    private var view: View? = null

    var pivot: CameraPivot = NO_CAMERA_PIVOT
    var snapToBounds: Rectangle = Rectangle()
        set(value) {snapToBounds(value)}
    var velocity: Float = 0.25f

    fun adjust() {
        val view = this.view ?: return

        if (getPos(view.data.zoom, view.data.bounds, view.data.worldPosition)) {
            view.data.worldPosition.x = floor(view.data.worldPosition.x.toDouble() + pos.x).toFloat()
            view.data.worldPosition.y = floor(view.data.worldPosition.y.toDouble() + pos.y).toFloat()
            ViewChangeEvent.send(view.componentId, ViewChangeEvent.Type.ORIENTATION)
        }
    }

    override fun update(componentId: CompId) {
        val view = this.view ?: return

        if (getPos(view.data.zoom, view.data.bounds, view.data.worldPosition)) {
            view.data.worldPosition.x += pos.x * velocity
            view.data.worldPosition.y += pos.y * velocity
            ViewChangeEvent.send(view.componentId, ViewChangeEvent.Type.ORIENTATION)
        }
    }

    private fun getPos(zoom: Float, viewBounds: Rectangle, worldPosition: PositionF): Boolean {
        if (pivot === NO_CAMERA_PIVOT)
            return false

        val following = pivot()
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
        pos.x = ceil(pos.x.toDouble() - worldPosition.x).toFloat()
        pos.y = floor(pos.y.toDouble() - worldPosition.y).toFloat()

        return pos.x != 0f || pos.y != 0f
    }

    companion object : SystemComponentSubType<Controller, SimpleCameraController>(Controller, SimpleCameraController::class) {
        override fun createEmpty() = SimpleCameraController()
    }
}