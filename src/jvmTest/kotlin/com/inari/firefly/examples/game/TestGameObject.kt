package com.inari.firefly.examples.game

import com.inari.firefly.BlendMode
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.composite.AttributedComposite
import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.game.tile.TileContactFormType
import com.inari.firefly.game.tile.TileMaterialType
import com.inari.firefly.game.tiled.TiledObjectComposite
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.physics.contact.EContact

class TestGameObject : TiledObjectComposite() {

    private var spriteId = NO_COMP_ID
    private var entityId = NO_COMP_ID

    override fun load() {
        spriteId = SpriteAsset.buildAndActivate {
            name = "objectSprite"
            texture("playerTex")
            textureRegion(7 * 16, 1 * 16, 16, 16)
        }
    }

    override fun activate() {
        entityId = Entity.buildAndActivate {
            withComponent(ETransform) {
                view(this@TestGameObject.viewRef)
                layer(this@TestGameObject.layerRef)
                position(4 * 16, 12 * 16)
            }
            withComponent(ESprite) {
                sprite(this@TestGameObject.spriteId)
                blend = BlendMode.NORMAL_ALPHA
            }
            withComponent(EContact) {
                bounds(0, 0, 16, 16)
                contactType = TileContactFormType.QUAD
                material = TileMaterialType.TERRAIN_SOLID
            }
        }
    }

    override fun deactivate() {
        TODO("Not yet implemented")
    }

    override fun unload() {
        TODO("Not yet implemented")
    }

    companion object : SystemComponentSubType<Composite, TestGameObject>(Composite, TestGameObject::class) {
        init { CompositeSystem.compositeTypeMapping[TestGameObject::class.simpleName!!] = this }
        override fun createEmpty() = TestGameObject()
    }
}