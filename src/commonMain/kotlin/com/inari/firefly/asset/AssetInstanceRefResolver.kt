package com.inari.firefly.asset

import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.IndexedInstantiable
import com.inari.firefly.core.component.IndexedInstantiableList
import com.inari.util.Consumer
import com.inari.util.Named
import com.inari.util.Supplier
import com.inari.util.indexed.Indexed

class AssetInstanceRefResolver(
    private val setter: Consumer<Int>,
    private val getter: Supplier<Int>
) {

    operator fun invoke(id: CompId) =
        setter( AssetSystem.assets[id.instanceId].instanceId)
    operator fun invoke(index: Int) =
        setter(AssetSystem.assets[index].instanceId)
    operator fun invoke(indexed: Indexed) =
        setter(AssetSystem.assets[indexed.index].instanceId)
    operator fun invoke(name: String) =
        setter(AssetSystem.assets[name].instanceId)
    operator fun invoke(named: Named) =
        setter(AssetSystem.assets[named.name].instanceId)
    operator fun invoke(indexedInstantiable: IndexedInstantiable) =
        setter(indexedInstantiable.instanceId)
    operator fun invoke(indexedInstantiableList: IndexedInstantiableList, index: Int) =
        setter(indexedInstantiableList.instanceId(index))

    var instanceId : Int
        get() = getter()
        set(value) = setter(value)
}