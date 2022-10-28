
plugins {
    kotlin("multiplatform") version "1.7.20"
    id("maven-publish")
}

group = "com.inari.firefly"
version = "0.3"

repositories {
    mavenCentral()
    maven ( url = "https://jitpack.io" )
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(LEGACY) {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.9.12")
                runtimeOnly("com.badlogicgames.gdx:gdx-platform:1.9.12:natives-desktop")
                implementation("com.squareup.moshi:moshi:1.12.0")
                implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))

            }
        }
        val jsMain by getting {
            dependencies {
                implementation(npm("bitset", "5.1.0"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation(npm("bitset", "5.1.0"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}


