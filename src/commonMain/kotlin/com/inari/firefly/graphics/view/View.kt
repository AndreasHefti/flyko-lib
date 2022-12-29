package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ViewData
import com.inari.util.event.Event
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class View private constructor(): ComponentNode(View), ViewData, Controlled {

    override var isBase = false
        internal set

    override val renderPassIndex: Int
        get() = renderPassTo.targetKey.instanceIndex
    @JvmField val renderPassTo = CReference(View)
    @JvmField var zPosition = 0
    override val bounds = Vector4i()
    override val  worldPosition = Vector2f()
    override val  clearColor = Vector4f( 0f, 0f, 0f, 1f )
    override val tintColor = Vector4f( 1f, 1f, 1f, 1f )
    override var  blendMode = BlendMode.NONE
    @JvmField val shader = CReference(Shader)
    override var zoom = 1.0f
    override var  fboScale = 1.0f

    override var shaderIndex = -1
        internal set

    override val controllerReferences = ControllerReferences(View)

    override fun initialize() {
        super.initialize()
        // for default none base view, set render pass to base view
        //if (!isBase)
            //renderPassTo(BASE_VIEW_KEY)
    }

    override fun load() {
        if (shader.targetKey !== NO_COMPONENT_KEY)
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
        withChild(Layer, configure)

    fun withShader(configure: (Shader.() -> Unit)): ComponentKey {
        val key = withChild(Shader, configure)
        shader(key)
        return key
    }

    companion object : ComponentSystem<View>("View") {
        override fun allocateArray(size: Int): Array<View?> = arrayOfNulls(size)
        override fun create() = View()

        val VIEW_CHANGE_EVENT_TYPE = Event.EventType("ViewChangeEvent")
        private val viewChangeEvent = ViewChangeEvent(VIEW_CHANGE_EVENT_TYPE)

        fun notifyViewChangeEvent(viewIndex: Int, type: ViewChangeEvent.Type, pixelPerfect: Boolean) {
            viewChangeEvent.viewIndex = viewIndex
            viewChangeEvent.type = type
            viewChangeEvent.pixelPerfect = pixelPerfect
            Engine.notify(viewChangeEvent)
        }

        fun createViewChangeEvent(viewIndex: Int, type: ViewChangeEvent.Type, pixelPerfect: Boolean): ViewChangeEvent {
            val viewChangeEvent = ViewChangeEvent(VIEW_CHANGE_EVENT_TYPE)
            viewChangeEvent.viewIndex = viewIndex
            viewChangeEvent.type = type
            viewChangeEvent.pixelPerfect = pixelPerfect
            return viewChangeEvent
        }

        val BASE_VIEW_KEY = View {
            autoActivation = true
            name = "BASE_VIEW$STATIC_COMPONENT_MARKER"
            bounds(0, 0, Engine.graphics.screenWidth, Engine.graphics.screenHeight)
            isBase = true
        }

        init {
            ViewSystemRenderer  // initialize the view system rendering
        }
    }

    class ViewChangeEvent(override val eventType: EventType) : Event<(ViewChangeEvent) -> Unit>() {

        enum class Type { POSITION, ORIENTATION, SIZE }

        var viewIndex: Int = -1
            internal set
        var type: Type = Type.POSITION
            internal set
        var pixelPerfect = false
            internal set

        override fun notify(listener: (ViewChangeEvent) -> Unit) = listener(this)

    }

}