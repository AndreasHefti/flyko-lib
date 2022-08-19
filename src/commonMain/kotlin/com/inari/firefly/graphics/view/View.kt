package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ViewData
import com.inari.util.ZERO_FLOAT
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class View private constructor(): Component(View), ViewData, ControlledComponent<View> {

    override var isBase = false
        internal set
    @JvmField var zPosition = 0
    override val bounds = Vector4i()
    override val  worldPosition = Vector2f()
    override val  clearColor = Vector4f( 0f, 0f, 0f, 1f )
    override val tintColor = Vector4f( 1f, 1f, 1f, 1f )
    override var  blendMode = BlendMode.NONE
    @JvmField val shader = CReference(Shader)
    override var zoom = 1.0f
    override val  fboScale = ZERO_FLOAT
    override val controllerReference: CReference = CReference(Control)

    override var shaderIndex = -1
        internal set

    override fun load() {
        shaderIndex = Asset.resolveAssetIndex(shader.targetKey)
        Engine.graphics.createView(this)
        super.load()
    }

    override fun dispose() {
        shaderIndex = -1
        Engine.graphics.disposeView(this.index)
        super.dispose()
    }

    fun withLayer(configure: (Layer.() -> Unit)): ComponentKey =
        withChild(Layer.builder, ChildLifeCyclePolicy.ACTIVATE, configure)

    override val componentType = Companion
    companion object : ComponentSystem<View>("View") {

        @JvmField val BASE_VIEW_KEY = View.createKey("BASE_VIEW")

        init {
            ViewSystemRenderer  // initialize the view system rendering
            createBaseView()
        }

        private fun createBaseView() {
            if (BASE_VIEW_KEY.instanceId >= 0) return

            View.buildAndGetActive {
                name = BASE_VIEW_KEY.name
                bounds(0, 0, Engine.graphics.screenWidth, Engine.graphics.screenHeight)
                isBase = true
            }
        }

        override fun clearSystem() {
            super.clearSystem()
            createBaseView()
        }

        override fun allocateArray(size: Int): Array<View?> = arrayOfNulls(size)
        override fun create() = View()
    }

}