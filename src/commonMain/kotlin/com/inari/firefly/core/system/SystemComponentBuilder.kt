package com.inari.firefly.core.system

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentBuilder
import com.inari.util.aspect.Aspect

abstract class SystemComponentBuilder<out C : SystemComponent> : ComponentBuilder<C>() {

    abstract val compAspect: Aspect

    val build: (C.() -> Unit) -> CompId = { configure ->
        val comp: C = createEmpty()
        comp.also(configure)
        FFContext.mapper<C>(compAspect).receiver()(comp)
        comp.internalInit()
        comp.componentId
    }

    val buildAndGet: (C.() -> Unit) -> C = { configure ->
        val comp: C = createEmpty()
        comp.also(configure)
        FFContext.mapper<C>(compAspect).receiver()(comp)
        comp.internalInit()
        comp
    }

    val buildAndActivate: (C.() -> Unit) -> CompId = { configure ->
        val comp: C = createEmpty()
        comp.also(configure)
        FFContext.mapper<C>(compAspect).receiver()(comp)
        comp.internalInit()
        FFContext.activate(comp.componentId)
        comp.componentId
    }

    val buildActivateAndGet: (C.() -> Unit) -> C = { configure ->
        val comp: C = createEmpty()
        comp.also(configure)
        FFContext.mapper<C>(compAspect).receiver()(comp)
        comp.internalInit()
        FFContext.activate(comp.componentId)
        comp
    }

}