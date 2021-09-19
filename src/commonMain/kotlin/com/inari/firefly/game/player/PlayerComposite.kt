package com.inari.firefly.game.player

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import com.inari.util.geom.PositionF
import kotlin.jvm.JvmField

class PlayerComposite internal constructor() {

    @JvmField internal val attributes = mutableMapOf<String, String>()
    @JvmField internal val loadedComponents = DynArray.of<CompId>(5, 10)
    @JvmField internal val activatableComponents = DynArray.of<CompId>(5, 10)

    fun getAttribute(name: String): String? = attributes[name]
    fun setAttribute(name: String, value: String) { attributes[name] = value }
    fun registerLoadedComponent(id: CompId, activatable: Boolean = false) {
        loadedComponents + id
        if (activatable)
            activatableComponents + id
    }

    var loaded = false
        internal set
    var active = false
        internal set
    var entityId = NO_COMP_ID
        internal set
    var playerPosition = PositionF()
        internal set
    var playerTransform = ETransform.create()
        internal set
    val aspects = PlayerSystem.PLAYER_ASPECT_GROUP.createAspects()

}