plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlin.serialization.json)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions)
}

tasks.test {
    useJUnitPlatform()
}