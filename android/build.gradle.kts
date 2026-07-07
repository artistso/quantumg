plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.quantumg"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.quantumg"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:1.12.0")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.0:natives-armeabi-v7a")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.0:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.0:natives-x86")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.0:natives-x86_64")
}