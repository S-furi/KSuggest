plugins {
    kotlin("jvm") version "2.1.10"
    application
}

application {
    mainClass.set("io.github.sfuri.KSuggestKt")
}

group = "io.github.sfuri"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
