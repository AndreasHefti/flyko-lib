package com.inari.firefly.asset

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynIntArray

import kotlin.jvm.JvmField

object AssetSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Asset)

    @JvmField
    val assets = ComponentSystem.createComponentMapping(
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
        if (dependingIndex >= 0 && !assets.isActive(dependingIndex)) {
            assets.activate(dependingIndex)
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
                .filter { assets.isActive(it) }
                .forEach { assets.deactivate(it) }
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
                .forEach { assets.delete(dependingAssetIds[it]) }
        }

        AssetEvent.send(
            id = asset.componentId,
            type = AssetEvent.Type.ASSET_DELETED
        )
    }

    override fun clearSystem() {
        assets.clear()
    }

    private fun findDependingAssets(assetId: Int) {
        dependingAssetIds.clear()
        assets.map
            .filter { it.dependsOn(assetId) }
            .forEach { dependingAssetIds.add(it.index) }
    }
}