plugins {
    kotlin("android")
    id("com.android.application")
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
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:1.12.0")
}
