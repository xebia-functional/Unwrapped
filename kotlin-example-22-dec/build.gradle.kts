plugins {
    kotlin("jvm") version "1.5.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.arrow-kt:arrow-core:1.0.1")
    implementation("io.arrow-kt:arrow-optics:1.0.1")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
}
