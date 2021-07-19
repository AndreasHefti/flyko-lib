/*******************************************************************************
 * Copyright (c) 2015 - 2016 - 2016, Andreas Hefti, inarisoft@yahoo.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inari.util.event

import com.inari.util.aspect.Aspect
import com.inari.util.collection.DynArray
import com.inari.util.collection.StaticList

/** A simple, synchronous and none thread save implementation of the [IEventDispatcher] interface.
 *
 * @author andreas hefti
 * @see IEventDispatcher
 */
class EventDispatcher : IEventDispatcher {

    private val eventLog: IEventLog?

    private val listeners: DynArray<StaticList<*>> = DynArray.of(10, 10)

    constructor() {
        eventLog = null
    }

    constructor(eventLog: IEventLog) {
        this.eventLog = eventLog
    }

    override fun <L> register(eventTypeAspect: Aspect, listener: L) {
        val listenersOfType = getListenersOfType<L>(eventTypeAspect, true)
        if (!listenersOfType.contains(listener))
            listenersOfType.add(listener)
    }

    override fun <L> unregister(eventTypeAspect: Aspect, listener: L): Boolean {
        val listenersOfType = getListenersOfType<L>(eventTypeAspect, false)
        return listenersOfType.remove(listener) >= 0
    }

    override fun <L> notify(event: Event<L>) {
        eventLog?.log(event)
        val listenersOfType = this.getListenersOfType<L>(event.eventType, false)
        val size = listenersOfType.capacity()
        var i = 0
        while (i < size) {
            val listener = listenersOfType[i]
            if (listener != null )
                event._notify(listener)
            i++
        }

        event._restore()
    }

    override fun <L : AspectedEventListener> notify(event: AspectedEvent<L>) {
        eventLog?.log(event)
        val listenersOfType = this.getListenersOfType<L>(event.eventType, false)
        val size = listenersOfType.capacity()
        var i = 0
        while (i < size) {
            val listener = listenersOfType[i]
            if (listener != null && listener.match(event.aspects))
                event._notify(listener)
            i++
        }
        event._restore()
    }

    override fun <L : PredicatedEventListener> notify(event: PredicatedEvent<L>) {
        eventLog?.log(event)
        val listenersOfType = this.getListenersOfType<L>(event.eventType, false)
        val size = listenersOfType.capacity()
        var i = 0
        while (i < size) {
            val listener = listenersOfType[i]
            if (listener != null) {
                val matcher = listener.getMatcher<L>()
                if (matcher(event))
                    event._notify(listener)
            }
            i++
        }

        event._restore()
    }

    override fun toString(): String =
        "EventDispatcher [listeners=$listeners]"

    @Suppress("UNCHECKED_CAST")
    private fun <L> getListenersOfType(eventTypeAspect: Aspect, create: Boolean): StaticList<L> {
        var listenersOfType: StaticList<L> = StaticList.emtpyList()
        if (listeners.contains(eventTypeAspect.aspectIndex))
            listenersOfType = listeners[eventTypeAspect.aspectIndex] as StaticList<L>
        else
            if (create) {
                listenersOfType = StaticList(10, 10)
                listeners[eventTypeAspect.aspectIndex] = listenersOfType
            }

        return listenersOfType
    }
}
