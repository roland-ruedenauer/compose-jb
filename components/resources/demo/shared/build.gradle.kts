import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    jvm("desktop")
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    js {
        browser {
            testTask(Action {
                enabled = false
            })
        }
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        val desktopMain by getting
        val wasmJsMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation("org.jetbrains.compose.material:material-icons-core:1.6.11")
            implementation(project(":resources:library"))
        }
        desktopMain.dependencies {
            implementation(compose.desktop.common)
        }
        androidMain.dependencies {
            implementation(libs.androidx.ui.tooling)
            implementation(libs.androidx.ui.tooling.preview)
        }

        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
            wasmJsMain.dependsOn(this)
            desktopMain.dependsOn(this)
            nativeMain.get().dependsOn(this)
            jsMain.get().dependsOn(this)
        }
    }
}

android {
    compileSdk = 35
    namespace = "org.jetbrains.compose.resources.demo.shared"
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

compose.experimental {
    web.application {}
}

//because the dependency on the compose library is a project dependency
compose.resources {
    generateResClass = always
}
