package com.inari.firefly.game.tiled

import com.inari.firefly.composite.Composite
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import kotlin.jvm.JvmField

abstract class TiledObjectComposite : Composite() {

    protected var viewRef = -1
    protected var layerRef = -1

    @JvmField var view = ComponentRefResolver(View) { index -> viewRef = index }
    @JvmField var layer = ComponentRefResolver(Layer) { index -> layerRef = index }
    @JvmField var tilesObjectProperties: TiledMapAsset.TiledObject = TiledMapAsset.TiledObject()
}