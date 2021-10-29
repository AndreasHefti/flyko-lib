# FlyKo-Lib
Firefly API for Kotlin multiplatform library
 


Introduction
------------

Firefly is a top-level 2D game framework. Focusing on intuitive API and build within stringent architecture and design 
principles like Component-Entity-System, Builder/DSL and component indexing for fast access.

The main goal of Firefly framework is to have a top-level 2D game API that comes with an in-build Component-Entity-System 
architecture that helps to organize all the game-objects, data and assets in a well-defined form and helps a lot 
on keeping the game code-base as flexible as possible for changes. Since almost everything in Firefly is a component,
to create them is following always the very same builder DSL while business-code stays in their System(s). 
This leads to more flexibility when content or behavior must be changed or need to be extended. 
Firefly also intent to be as less restrictive as possible to make it easy to integrate other libraries or just go
your own path for particular section if you want to do so. Firefly shall help you with that and not strain you with
stupid conventions.

Firefly's Entity-Component-System is implemented on-top of a small set of interfaces that defines the API to the
lower-level libraries that can be used to render graphics, play sounds or gather or poll input events.
This is a Kotlin multiplatform library project with the usual four main modules:

- **common**: where the Firefly framework lives

- **js**: Kotlin JavaScript implementation (not done yet)

- **jvm**: Kotlin implementation to run on a JVM for desktop or mobile (done for desktop with lwjgl/libgdx as lower level framework)

- **native**: Kotlin native implementations (not done yet)

Currently, only the JVM part for desktop (Windows/Mac/Linux) is implemented and fully working and 
building and packaging apps and games for desktop (Win, Mac and Linux) is currently supported


Key Features 
-----------------

**Firefly framework (Kotlin common)**

- Strong backing on components and Component-Entity-System approach.

- Lightweight but powerful and easy extendable event system for communication between Systems.

- Independent lower level interface definition.

- Stringent component builder API with DSL support.

- Indexing for component types and instances for fast access

- Multiple Views by using render-to-texture with FBO - Framebuffer Object.

- OpenGL shader support.

- Basic libraries for input, audio, tile-maps, animation, movement, contact/collision, particle, action, state, behavior and many more.

- Extendable by design. Easily add and integrate third party libraries for physics or other needs within your game.  

- Simple and concise lower level API for additional Kotlin multiplatform implementations

**Kotlin JVM implementation**

- Ready to use JVM implementation available over Jitpack 

- JVM implementation that can be used for Desktop apps (Win,Mac,Linux)

- TODO: JVM implementation that can be used for Android development

**Kotlin JS implementation**

- TODO

**Kotlin Natives implementation**

- TODO

### [Project Board](https://view.monday.com/1824751381-dbd29f0e327df610ad7628d957a2afcd?r=use1)

Code Example
--------------
<div align="center"><img src="https://github.com/Inari-Soft/flyKo/raw/master/wiki/example1.gif" alt="Result of Code Example"></div>

``` kotlin
  // Create a TextureAsset and register it to the AssetSystem but not loading yet.
  //
  // WIth this method you are able to define all your assets in one place without
  // loading the assets into memory yet. When you need them you can simply load
  // them by calling FFContext.activate(TextureAsset, "logoTexture") and dispose them
  // with FFContext.dispose(TextureAsset, "logoTexture"). The asset definition is still
  // available and can be deleted with FFContext.delete(TextureAsset, "logoTexture")
  val texAssetId = TextureAsset.build {
      name = "logoTexture"
      resourceName = "firefly/logo.png"
  }

  // Create and activate/load a SpriteAsset with reference to the TextureAsset.
  // This also implicitly loads the TextureAsset if it is not already loaded.
  val spriteId = SpriteAsset.buildAndActivate {
      // It would also be possible to use the name of the texture asset here
      // instead of the identifier. But of corse, identifier (index) gives faster access
      texture(texAssetId)
      textureRegion(0,0,32,32)
      horizontalFlip = false
      verticalFlip = false
  }

  // Create an Entity positioned on the base View on x=50/y=150, and the formerly
  // created sprite with a tint color.
  // Add also two animation, one for the alpha of the tint color and one for the
  // position on the x axis and activate everything immediately.
  val entityId = Entity.buildAndActivate {

      // add a transform component to the entity that defines the orientation of the Entity
      component(ETransform) {
          view(BASE_VIEW)
          pPosition(50, 150)
          scale(4f, 4f)
      }

      // add a sprite component to the entity
      component(ESprite) {
          sprite(spriteId)
          tint(1f, 1f, 1f, .5f)
      }

      // add an animation component to the entity that defines an animation based on
      // the alpha value of the color property of the sprite.
      //
      // Animations normally can work for itself and lifes in the AnimationSystem. But if
      // a property of an Entity-Component like ESprite defines a property value adapter,
      // an animation can be bound to this property directly to affecting the value of the property.
      component(EAnimation) {

          // with an active easing animation on the sprite alpha blending value...
          activeAnimation(EasedProperty) {
              easing = Easing.Type.LINEAR
              startValue = 0f
              endValue = 1f
              duration = 3000
              looping = true
              inverseOnLoop = true

              // that is connected to the alpha value of the sprite of the entity
              propertyRef = ESprite.Property.TINT_ALPHA
          }

          // and with an active easing animation on the sprites position on the x axis...
          activeAnimation(EasedProperty) {
              easing = Easing.Type.BACK_OUT
              startValue = 50f
              endValue = 400f
              duration = 1000
              looping = true
              inverseOnLoop = true

              // that is connected to the position value on the x axis of the entities transform data
              propertyRef = ETransform.Property.POSITION_X
          }
      }
  }
```



Let's Get Started with Fly-Ko
------------------------------

TODO - Dokumentation

Install
-------

*Create project for a JVM based application for desktop (Win,Mac,Linux)*

#### Maven

``` xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.AndreasHefti</groupId>
    <artifactId>flyko-lib</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

#### Gradle (KTS)

``` kotlin

repositories {
    maven ( url = "https://jitpack.io" )
}

dependencies {
    implementation("com.github.AndreasHefti.flyko-lib:flyko-lib:master-SNAPSHOT")
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:1.9.12:natives-desktop")
}

```

License
--------

Firefly is licensed under the Apache 2 License, meaning you can use it free of charge, 
without strings attached in commercial and non-commercial projects.




