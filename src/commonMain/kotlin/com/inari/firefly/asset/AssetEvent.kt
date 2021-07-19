package com.inari.firefly.asset

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.util.Consumer
import com.inari.util.event.Event

class AssetEvent(override val eventType: EventType) : Event<Consumer<AssetEvent>>() {

    enum class Type {
        ASSET_CREATED,
        ASSET_LOADED,
        ASSET_DISPOSED,
        ASSET_DELETED
    }

    var assetId: CompId = NO_COMP_ID
        internal set
    var type: Type = Type.ASSET_CREATED
        internal set

    override fun notify(listener: Consumer<AssetEvent>) = listener(this)

    companion object : EventType("AssetEvent") {
        internal val assetEvent = AssetEvent(this)
        fun send(id: CompId, type: Type) {
            assetEvent.assetId = id
            assetEvent.type = type
            FFContext.notify(assetEvent)
        }
    }
}