package com.inari.firefly.game.world

import com.inari.firefly.asset.Asset
import com.inari.firefly.control.task.Task
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

enum class WorldOrientationType {
    TILES,
    PIXELS,
    SECTION,
    COUNT
}
abstract class WorldObject protected constructor(
    objectIndexerName: String
) : SystemComponent(objectIndexerName) {

    @JvmField internal val attributes = mutableMapOf<String, String>()
    @JvmField internal val assetRefs = BitSet()
    @JvmField internal val activationTaskRefs = BitSet()

    @JvmField var orientationType: WorldOrientationType = WorldOrientationType.COUNT
    @JvmField val orientation: Vector4i = Vector4i()

    @JvmField internal val loadedComponents = DynArray.of<CompId>(5, 10)

    var parentRef = -1
        internal set
    val withParent = ComponentRefResolver(World) { index -> parentRef + index }

    fun getAttribute(name: String): String? = attributes[name]
    fun setAttribute(name: String, value: String) { attributes[name] = value }

    val withAsset = ComponentRefResolver(Asset) { index -> assetRefs.set(index) }
    val withActivationTask = ComponentRefResolver(Task) { index -> activationTaskRefs.set(index) }
    fun registerLoadedComponent(id: CompId) = loadedComponents + id

}