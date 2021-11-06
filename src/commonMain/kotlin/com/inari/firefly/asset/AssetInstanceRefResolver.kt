package com.inari.firefly.asset

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.IndexedInstantiable
import com.inari.firefly.core.component.IndexedInstantiableList
import com.inari.util.Consumer
import com.inari.util.Named
import com.inari.util.Supplier
import com.inari.util.indexed.Indexed
import kotlin.jvm.JvmField

/** Used to resolve an Asset instance identifier form a loaded Asset.
 *  This is usually used on component fields that refers to an Asset and implements
 *  several invoke methods to get the Assets instance identifier reference.
 *  </p>
 *  For default the invoke throws a RuntimeException if the Asset that is been references
 *  is not loaded or active and has a negative instanceId.
 *
 *  <pre>
 *
 *      // implementation
 *      internal var assetRefId: Int = -1
 *      val assetRef = AssetInstanceRefResolver(
 *          { assetInstanceId -> assetRefId = assetInstanceId },
 *          { assetRefId }
 *      )
 *
 *      // usage
 *      component.assetRef(1)
 *      component.assetRef("AssetName")
 *      component.assetRef(assetId)
 *      component.assetRef(componentId)
 *
 *  </pre>
 */
class AssetInstanceRefResolver(
    private val setter: Consumer<Int>,
    private val getter: Supplier<Int>
) {

    operator fun invoke(id: CompId) =
        setter(checkInstance(AssetSystem.assets[id.instanceId]))
    operator fun invoke(index: Int) =
        setter(checkInstance(AssetSystem.assets[index]))
    operator fun invoke(indexed: Indexed) =
        setter(checkInstance(AssetSystem.assets[indexed.index]))
    operator fun invoke(name: String) =
        setter(checkInstance(AssetSystem.assets[name]))
    operator fun invoke(named: Named) =
        setter(checkInstance(AssetSystem.assets[named.name]))
    operator fun invoke(indexedInstantiable: IndexedInstantiable) =
        setter(checkInstanceId(indexedInstantiable.instanceId))
    operator fun invoke(indexedInstantiableList: IndexedInstantiableList, index: Int) =
        setter(checkInstanceId(indexedInstantiableList.instanceId(index)))

    var instanceId : Int
        get() = getter()
        set(value) = setter(value)

    @JvmField var loadAssetWhenInactive: Boolean = false

    private fun checkInstance(asset: Asset): Int {
        if (asset.instanceId < 0)
            if (loadAssetWhenInactive)
                FFContext.activate(asset)
            else
                throw RuntimeException("Asset with id: ${asset.componentId} name: ${asset.name} is not loaded or active!")

        return asset.instanceId
    }

    private fun checkInstanceId(instanceId: Int): Int {
        if (instanceId < 0)
            throw RuntimeException("InstanceId is negative!")
        return instanceId
    }
}