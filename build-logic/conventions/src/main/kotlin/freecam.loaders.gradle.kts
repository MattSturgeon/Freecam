plugins {
    id("freecam.common")
    id("freecam.publish")
}

dependencies {
    implementation(commonNode.project)
}

publishing {
    repositories {
        mavenLocal()
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}