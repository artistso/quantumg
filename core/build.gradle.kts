plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    val gdxVersion = "1.12.0"

    api("com.badlogicgames.gdx:gdx:$gdxVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Math
    implementation("org.ejml:ejml-all:0.43.1")
    implementation("space.kscience:kmath-core:0.3.0")
    implementation("space.kscience:kmath-commons:0.3.0")
    implementation("com.github.jksalcedo:kotlin-math-lib:1.0.0")

    // Geometry (NURBS curves)
    implementation("com.github.virtuald:curvesapi:1.06")

    // Graph Theory
    implementation("io.github.alexandrepiveteau:kotlin-graphs:0.6.0")

    // QKD (quantum key distribution simulation)
    implementation("com.github.johanvos:qkd-java:main-SNAPSHOT")

    // KotlinLab for math plotting
    implementation("com.github.sterglee:KotlinLabSimple:1.0.0")

    // XTrain (topology)
    // implementation("com.github.xtrain:xtrain:...")
}
