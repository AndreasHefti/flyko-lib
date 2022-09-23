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

Code Example
--------------
<div align="center"><img src="https://github.com/Inari-Soft/flyKo/raw/master/wiki/example1.gif" alt="Result of Code Example"></div>

``` kotlin
fun main(args: Array<String>) {
    DesktopApp( "CoverCodeTest", 800, 600) {

        // Create a Texture and register it to the AssetSystem but not loading yet.
        Texture {
            name = "logoTexture"
            resourceName = "firefly/logo.png"
            // Create  a Sprite with reference to the Texture.
            // This also implicitly loads the Texture on sprite load if it is not already loaded.
            withChild(Sprite) {
                name = "inariSprite"
                textureBounds(0, 0, 32, 32)
                hFlip = false
                vFlip = false
            }
        }
        // Create an Entity positioned on the base View on x=50/y=150, and the formerly
        // created sprite with a tint color. This also automatically loads the needed assets if not already done
        Entity {
            // automatically activate the entity after creation
            autoActivation = true
            // add a transform component to the entity that defines the orientation of the Entity
            withComponent(ETransform) {
                viewRef(View.BASE_VIEW_KEY)
                position(50, 150)
                scale(4f, 4f)
            }
            // add a sprite component to the entity
            withComponent(ESprite) {
                spriteRef("inariSprite")
                tintColor(1f, 1f, 1f, .5f)
            }
            withComponent(EAnimation) {
                // with an active easing animation on the sprite alpha blending value...
                withAnimation(EasedFloatAnimation) {
                    looping = true
                    inverseOnLoop = true
                    easing = Easing.LINEAR
                    startValue = 0f
                    endValue = 1f
                    duration = 3000
                    animatedProperty = ESprite.PropertyAccessor.TINT_COLOR_ALPHA
                }
                // and with an active easing animation on the sprites position on the x axis...
                withAnimation(EasedFloatAnimation) {
                    looping = true
                    inverseOnLoop = true
                    easing = Easing.BACK_OUT
                    startValue = 50f
                    endValue = 400f
                    duration = 1000
                    animatedProperty = ETransform.PropertyAccessor.POSITION_X
                    animationController(DefaultFloatEasing)
                }
            }
        }
    }
}
```



Let's Get Started with Fly-Ko
------------------------------

TODO - Documentation

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




