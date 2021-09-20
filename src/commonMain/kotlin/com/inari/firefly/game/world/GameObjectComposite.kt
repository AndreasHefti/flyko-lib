package com.inari.firefly.game.world

import com.inari.firefly.composite.Composite
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.game.json.TiledJsonRoomAsset
import com.inari.firefly.game.json.TiledObject
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import kotlin.jvm.JvmField


abstract class GameObjectComposite : Composite() {

    protected var viewRef = -1
    protected var layerRef = -1

    @JvmField var view = ComponentRefResolver(View) { index -> viewRef = index }
    @JvmField var layer = ComponentRefResolver(Layer) { index -> layerRef = index }
    @JvmField var tilesObjectProperties: TiledObject = TiledObject()
}