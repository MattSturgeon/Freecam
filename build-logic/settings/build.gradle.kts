import net.xolt.freecam.gradle.plugin

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":api"))
    implementation(libs.asm)
    implementation(libs.asm.commons)
    implementation(plugin(libs.plugins.stonecutter))
    implementation(plugin(libs.plugins.foojay.resolver))
    implementation(libs.kotlin.serialization.toml)
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.kikugie.dev/snapshots")
}

gradlePlugin {
    plugins {
        create("modMetadata") {
            id = "freecam.modmetadata"
            implementationClass = "net.xolt.freecam.gradle.ModMetadataSettingsPlugin"
        }
        create("modLibraries") {
            id = "freecam.modlibraries"
            implementationClass = "net.xolt.freecam.gradle.ModLibrariesSettingsPlugin"
        }
    }
}