plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val gdxVersion = "1.12.0"

    // LibGDX core
    api("com.badlogicgames.gdx:gdx:$gdxVersion")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Math (EJML — pure Java matrix library, well-supported)
    implementation("org.ejml:ejml-all:0.43.1")

    // Graph Theory
    implementation("io.github.alexandrepiveteau:kotlin-graphs:0.6.0")
}