plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":api"))
    implementation(libs.plugins.stonecutter.coords)
    implementation(libs.plugins.foojay.resolver.coords)
    implementation(libs.kotlin.serialization.toml)
}

gradlePlugin {
    plugins {
        create("ideaSync") {
            id = "freecam.idea.sync"
            implementationClass = "net.xolt.freecam.gradle.IdeaSyncPlugin"
        }
        create("modMetadata") {
            id = "freecam.modmetadata"
            implementationClass = "net.xolt.freecam.gradle.ModMetadataSettingsPlugin"
        }
    }
}