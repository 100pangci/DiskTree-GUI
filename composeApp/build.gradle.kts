import org.jetbrains.compose.desktop.application.dsl.TargetFormat

fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget()
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation("androidx.activity:activity-compose:1.9.2")
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
        }
    }
}

android {
    namespace = "io.github.disktreegui"
    compileSdk = 34

    val keystorePath = env("ANDROID_KEYSTORE_PATH")
    val keystorePassword = env("ANDROID_KEYSTORE_PASSWORD")
    val keyAlias = env("ANDROID_KEY_ALIAS")
    val keyPassword = env("ANDROID_KEY_PASSWORD")

    defaultConfig {
        applicationId = "io.github.disktreegui"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        if (keystorePath != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.disktreegui.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DiskTree GUI"
            packageVersion = "1.0.0"
            modules("java.base", "java.desktop", "java.logging", "java.sql")
            linux { iconFile.set(project.file("src/desktopMain/resources/app_icon.png")) }
            windows { iconFile.set(project.file("src/desktopMain/resources/app_icon.png")) }
            macOS { iconFile.set(project.file("src/desktopMain/resources/app_icon.png")) }
        }
    }
}