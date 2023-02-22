package examples

import com.badlogic.gdx.Input
import com.inari.firefly.DesktopApp
import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.util.geom.Vector2f

fun main() {
    DesktopApp("LayerControlTest", 800, 600) {

        Texture.build {
            name = "logoTexture"
            resourceName = "firefly/logo.png"
            withSprite {
                name = "inariSprite1"
                textureBounds(0, 0, 32, 32)
                hFlip = false; vFlip = false
            }
            withSprite {
                name = "inariSprite2"
                textureBounds(0, 0, 32, 32)
                hFlip = true; vFlip = true
            }
        }

        val viewKey = View {
            autoActivation = true
            name = "view1"
            bounds(0, 0, 800, 600)
            blendMode = BlendMode.NORMAL_ALPHA
            zoom = 0.5f
            withLayer { name = "layer1"; withControl(LayerControl) { name = "controller1" } }
            withLayer { name = "layer2"; position(50, 102) ; withControl(LayerControl) { name = "controller2" }}
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) { viewRef(viewKey); layerRef("layer1") }
            withComponent(ESprite) { spriteRef("inariSprite1") }
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) { viewRef(viewKey); layerRef("layer2") }
            withComponent(ESprite) { spriteRef("inariSprite2") }
        }

        ComponentSystem.dumpInfo()

        it.addExitKeyTrigger(Input.Keys.SPACE)
        it.onDispose = {
            println("****************************************** onDispose")
            View.delete(viewKey)
            Texture.delete("logoTexture")
            ComponentSystem.dumpInfo()
        }
    }
}

class LayerControl : SingleComponentControl<Layer>(Layer) {

    private val move = Vector2f(1f, 1f)

    override fun update(c: Layer) {
        c.position + this.move
        if (c.position.x >= 400- 32) this.move.x = -1f
        if (c.position.x <= 0) this.move.x = 1f
        if (c.position.y >= 300 - 32) this.move.y = -1f
        if (c.position.y <= 0) this.move.y = 1f
    }


    companion object : ComponentSubTypeBuilder<Control, LayerControl>(Control, "LayerControl") {
        override fun create() = LayerControl()
    }


}