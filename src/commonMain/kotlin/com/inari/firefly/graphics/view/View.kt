package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ViewData
import com.inari.util.event.Event
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField
import kotlin.math.roundToInt

class View private constructor(): Composite(View), ViewData, Controlled {

    override var isBase = false
        internal set

    override val clearBeforeStartRendering = true
    @JvmField var zPosition = 0
    override val bounds = Vector4i()
    override val worldPosition = Vector2f()
    override val clearColor = Vector4f( 0f, 0f, 0f, 1f )
    override val tintColor = Vector4f( 1f, 1f, 1f, 1f )
    override var blendMode = BlendMode.NONE
    override var zoom = 1.0f
    override var  fboScale = 1.0f
    @JvmField val shader = CReference(Shader)
    override var shaderIndex = -1
        internal set

    @JvmField var excludeFromEntityRendering = false
    override var renderTargetOf1 = -1
        internal set
    override var renderTargetOf2 = -1
        internal set
    override var renderTargetOf3 = -1
        internal set
    override var renderToBase = true
        set(value) {
            val reload = field != value && active
            field = value
            if (reload) {
                View.deactivate(this)
                View.activate(this)
            }
        }

    fun withLayer(configure: (Layer.() -> Unit)): ComponentKey  {
        val layer = Layer.buildAndGet(configure)
        layer.viewRef(earlyKeyAccess())
        return layer.key
    }

    fun withShader(configure: (Shader.() -> Unit)): ComponentKey {
        shader(Shader.build(configure))
        return shader.targetKey
    }

    fun asRenderTargetOf(configure: (View.() -> Unit)) : ComponentKey {
        val c = View.buildAndGet(configure)
        c.renderToBase = false
        if (renderTargetOf1 < 0) {
            renderTargetOf1 = c.key.instanceIndex
            return c.key
        }
        if (renderTargetOf2 < 0) {
            renderTargetOf2 = c.key.instanceIndex
            return key
        }
        if (renderTargetOf3 < 0) {
            renderTargetOf3 = c.key.instanceIndex
            return key
        }
        View.delete(c)
        throw IllegalStateException("All three render targets are already set.")
    }

    override val controllerReferences = ControllerReferences(View)

    override fun load() {
        if (shader.targetKey !== NO_COMPONENT_KEY)
            shaderIndex = Asset.resolveAssetIndex(shader.targetKey)
        Engine.graphics.createView(this)
        super.load()

        // load dependent layers
        Layer.forEachDo {
            if (it.viewRef.targetKey == this.key)
                Layer.load(it)
        }
    }

    override fun deactivate() {
        // deactivate dependent layers first
        Layer.forEachDo {
            if (it.viewRef.targetKey == this.key)
                Layer.deactivate(it)
        }

        super.deactivate()
    }

    override fun dispose() {
        // dispose dependent layers first
        Layer.forEachDo {
            if (it.viewRef.targetKey == this.key)
                Layer.dispose(it)
        }

        shaderIndex = -1
        Engine.graphics.disposeView(this.index)
        super.dispose()
    }

    override fun delete() {
        // delete dependent layers first
        Layer.forEachDo {
            if (it.viewRef.targetKey == this.key)
                Layer.delete(it)
        }
        super.delete()
    }

    companion object : ComponentSystem<View>("View") {
        override fun allocateArray(size: Int): Array<View?> = arrayOfNulls(size)
        override fun create() = View()

        val VIEW_CHANGE_EVENT_TYPE = Event.EventType("ViewChangeEvent")
        private val viewChangeEvent = ViewChangeEvent(VIEW_CHANGE_EVENT_TYPE)

        val BASE_VIEW_KEY = View {
            autoActivation = true
            name = "BASE_VIEW$STATIC_COMPONENT_MARKER"
            bounds(0, 0, Engine.graphics.screenWidth, Engine.graphics.screenHeight)
            isBase = true
        }

        val BASE_VIEW = View[BASE_VIEW_KEY]

        init {
            ViewSystemRenderer  // initialize the view system rendering
        }

        var fitBaseViewPortToScreen: Boolean = true
        var centerCamera: Boolean = true
        val baseViewPortProjectionSize = Vector2f(
            Engine.graphics.screenWidth,
            Engine.graphics.screenHeight)

        fun notifyScreenSizeChange(
            width: Int,
            height: Int,
            baseWidth: Int,
            baseHeight: Int) {

            if (width <= 0 || height <= 0)
                return
            val bounds = BASE_VIEW.bounds
            val worldPosition = BASE_VIEW.worldPosition
            val targetRatio = height.toFloat() / width
            val sourceRatio = baseHeight.toFloat() / baseWidth
            val fitToWidth = targetRatio > sourceRatio
            val zoom = BASE_VIEW.zoom

            if (fitToWidth) {
                bounds.width = baseWidth
                bounds.height = (baseHeight / sourceRatio * targetRatio).roundToInt()
                baseViewPortProjectionSize.v0 = Engine.graphics.screenWidth.toFloat()
                baseViewPortProjectionSize.v1 = Engine.graphics.screenWidth * sourceRatio
            } else {
                bounds.width = (baseWidth / targetRatio * sourceRatio).roundToInt()
                bounds.height = baseHeight
                baseViewPortProjectionSize.v0 = Engine.graphics.screenHeight / sourceRatio
                baseViewPortProjectionSize.v1 = Engine.graphics.screenHeight.toFloat()
            }

            if (centerCamera) {
                worldPosition.x = -(bounds.width - baseWidth).toFloat() / 2 * zoom
                worldPosition.y = -(bounds.height - baseHeight).toFloat() / 2 * zoom
            }
        }

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