package com.inari.firefly.graphics.rendering

import com.inari.firefly.FFApp.RenderEvent
import com.inari.firefly.FFContext
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.FFSystem
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.util.Consumer
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object RenderingSystem : FFSystem {

    @JvmField internal val renderer =
        ComponentSystem.createComponentMapping(Renderer)

    val properties = Properties()

    private val renderingListener: Consumer<RenderEvent> = { e ->
        var i = 0
        while (i < properties.renderingChain.size)
            properties.renderingChain[i++].render(e.viewIndex, e.layerIndex, e.clip)
    }

    private val entityActivationListener: EntityEventListener = object : EntityEventListener {
        override fun entityActivated(entity: Entity) = _entityActicated(entity)
        override fun entityDeactivated(entity: Entity) = _entitiyDeactivated(entity)
        override fun match(aspects: Aspects) = true
    }


    init {
        FFContext.registerListener(RenderEvent, renderingListener)
        FFContext.registerListener(EntityEvent, entityActivationListener)
        setDefaultRenderingChain()
    }

    operator fun get(rendererKey: SingletonComponent<Renderer, out Renderer>): Renderer =
        renderer[rendererKey.instance.index]

    fun setDefaultRenderingChain() {
        setRenderingChain(
            SimpleTileGridRenderer,
            MultiPositionSpriteRenderer,
            SimpleSpriteRenderer,
            SpriteGroupRenderer,
            SimpleShapeRenderer,
            SimpleTextRenderer
        )
    }

    fun setRenderingChain(vararg renderingChain: SingletonComponent<Renderer, out Renderer>) {
        renderer.clear()
        properties.renderingChain = Array(renderingChain.size) {
            val r: Renderer = renderingChain[it].instance
            renderer.receiver()(r)
            r
        }
    }

    private fun _entityActicated(entity: Entity) {
        var i = 0
        while (i < properties.renderingChain.size) {
            val renderer = properties.renderingChain[i++]
            if (renderer.match(entity)) {
                if (renderer.accept(entity) && !properties.allowMultipleAcceptance)
                    return
            }
        }
    }

    private fun _entitiyDeactivated(entity: Entity) {
        var i = 0
        while (i < properties.renderingChain.size) {
            val renderer = properties.renderingChain[i++]
            if (renderer.match(entity))
                renderer.dispose(entity)

        }
    }

    override fun clearSystem() {}

    class Properties internal constructor() {
        @JvmField var allowMultipleAcceptance: Boolean = false
        internal var renderingChain: Array<Renderer> = emptyArray()
    }
}