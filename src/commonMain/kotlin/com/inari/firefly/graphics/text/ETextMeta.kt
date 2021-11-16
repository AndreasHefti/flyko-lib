package com.inari.firefly.graphics.text

import com.inari.firefly.core.component.ArrayAccessor
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class ETextMeta private constructor() : EntityComponent(ETextMeta::class.simpleName!!) {

    @JvmField internal val metaData: DynArray<CharacterMetaData> = DynArray.of()
    @JvmField var data = ArrayAccessor(metaData)
    @JvmField var resolver: (Int) -> CharacterMetaData? = { index -> metaData[index] }

    override fun reset() {
        metaData.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<ETextMeta>(ETextMeta::class) {
        override fun createEmpty() = ETextMeta()
    }
}