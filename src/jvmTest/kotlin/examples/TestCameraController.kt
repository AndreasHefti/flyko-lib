package examples

import com.inari.firefly.core.*
import com.inari.firefly.graphics.view.View

class TestCameraController private constructor() : SystemControl(View) {

    private lateinit var view: View
    private lateinit var viewChangeEvent: View.ViewChangeEvent
    private var up = false
    private var right = false
    private var down = false
    private var left = false

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

    override fun update(index: Int) {
        if (up) view.worldPosition.y -= 1f
        if (right) view.worldPosition.x += 1f
        if (down) view.worldPosition.y += 1f
        if (left) view.worldPosition.x -= 1f
        Engine.notify(viewChangeEvent)
    }

    override fun matchForControl(key: ComponentKey): Boolean {
        val comp = View[key]
        if (this.index in comp.controllerReferences) {
            this.view = View[key]
            viewChangeEvent = View.createViewChangeEvent(view.index, View.ViewChangeEvent.Type.ORIENTATION, false)
            return true
        }
        return false
    }

    companion object : ComponentSubTypeBuilder<Control, TestCameraController>(Control, "TestCameraController") {
        override fun create() = TestCameraController()
    }
}