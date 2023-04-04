package examples

import com.inari.firefly.core.*
import com.inari.firefly.graphics.view.View

class TestCameraController private constructor() : SingleComponentControl<View>(View) {


    private lateinit var viewChangeEvent: View.ViewChangeEvent
    private var up = false
    private var right = false
    private var down = false
    private var left = false

    override fun register(key: ComponentKey) {
        super.register(key)
        viewChangeEvent = View.createViewChangeEvent(
            key.componentIndex,
            View.ViewChangeEvent.Type.ORIENTATION,
            false)
    }

    override fun initialize() {
        super.initialize()
        Engine.input.setKeyCallback { k, s, a ->
            when (k) {
                265 -> up = a > 0
                262 -> right = a > 0
                264 -> down = a > 0
                263 -> left = a > 0
            }
        }
    }

    override fun update(c: View) {
        if (up) c.worldPosition.y -= 1f
        if (right) c.worldPosition.x += 1f
        if (down) c.worldPosition.y += 1f
        if (left) c.worldPosition.x -= 1f
        Engine.notify(viewChangeEvent)
    }

    companion object : SubComponentBuilder<Control, TestCameraController>(Control) {
        override fun create() = TestCameraController()
    }
}