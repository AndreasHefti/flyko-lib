package com.inari.firefly.asset

import com.inari.firefly.FFContext
import com.inari.firefly.core.ComponentRefPredicate
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynIntArray

object AssetSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Asset)

    val assets: ComponentMapRO<Asset>
        get() = systemAssets
    internal val systemAssets = ComponentSystem.createComponentMapping(
        Asset,
        activationMapping = true,
        nameMapping = true,
        listener = { asset, action  -> when (action) {
            CREATED       -> created(asset)
            ACTIVATED     -> load(asset)
            DEACTIVATED   -> unload(asset)
            DELETED       -> deleted(asset)
        } }
    )

    val loaderDispatcher = ComponentSystem.createLoaderDispatcher(
        Asset,
        ComponentRefResolver(Asset) { systemAssets.activate(it) },
        ComponentRefPredicate(Asset) { systemAssets.isActive(it) },
        ComponentRefResolver(Asset) { systemAssets.deactivate(it) }
    )

    private val dependingAssetIds: DynIntArray = DynIntArray(1, -1)

    init {
        FFContext.loadSystem(this)
    }

    private fun created(asset: Asset) {
        AssetEvent.send(
            id = asset.componentId,
            type = AssetEvent.Type.ASSET_CREATED
        )
    }

    private fun load(asset: Asset) {
        val dependingIndex = asset.dependingIndex()
        if (dependingIndex >= 0 && !systemAssets.isActive(dependingIndex)) {
            systemAssets.activate(dependingIndex)
        }

        asset.activate()
        AssetEvent.send(
            id = asset.componentId,
            type = AssetEvent.Type.ASSET_LOADED
        )

    }

    private fun unload(asset: Asset) {
        findDependingAssets(asset.index)
        if (!dependingAssetIds.isEmpty) {
            (0 until dependingAssetIds.length)
                .filterNot { dependingAssetIds.isEmpty(it) }
                .map { dependingAssetIds[it] }
                .filter { systemAssets.isActive(it) }
                .forEach { systemAssets.deactivate(it) }
        }

        asset.deactivate()
        AssetEvent.send(
            id = asset.componentId,
            type = AssetEvent.Type.ASSET_DISPOSED
        )
    }

    private fun deleted(asset: Asset) {
        if (dependingAssetIds.isEmpty) {
            (0 until dependingAssetIds.length)
                .filterNot { dependingAssetIds.isEmpty(it) }
                .forEach { systemAssets.delete(dependingAssetIds[it]) }
        }

        AssetEvent.send(
            id = asset.componentId,
            type = AssetEvent.Type.ASSET_DELETED
        )
    }

    override fun clearSystem() {
        systemAssets.clear()
    }

    private fun findDependingAssets(assetId: Int) {
        dependingAssetIds.clear()
        systemAssets.map
            .filter { it.dependsOn(assetId) }
            .forEach { dependingAssetIds.add(it.index) }
    }
}