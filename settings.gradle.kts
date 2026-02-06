import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.xolt.freecam.model.RelationshipType

val isCi = System.getenv("CI") == "true"
gradle.startParameter.isParallelProjectExecutionEnabled = !isCi
gradle.startParameter.isBuildCacheEnabled = !isCi
gradle.startParameter.isConfigureOnDemand = !isCi

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.firstdark.dev/releases/")
        maven("https://maven.firstdark.dev/snapshots/")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
    }
    includeBuild("build-logic")
}

plugins {
    id("freecam.settings")
    id("freecam.modmetadata")
    id("freecam.modlibraries")
}

// Convenience extensions
// FIXME: provide easier access to relevant data in the ModLibrarySpec scope?
private val Project.sc get() = extensions.getByType<StonecutterBuildExtension>()
private val Project.mc get() = sc.current.version
private val Project.loader get() = sc.branch.id

// TODO: Move metadata to a convention plugin
// Maybe with a top-level symlink for convenience?
modLibraries {

    runtime("minecraft") {
        group = "com.mojang"
        name = "minecraft"
        version = project.mc

        requires {
            maven = when (project.mc) {
                "1.16.5" -> "[1.16.2,1.17)"
                "1.17.1" -> "[1.17.1,1.18)"
                "1.18.2" -> "[1.18.2,1.19)"
                "1.19.4" -> "[1.19.4,1.20)"
                "1.20.6" -> "[1.20.6,)"
                "1.21.11" -> "[1.21.11,)"
                else -> error("Unsupported MC version: ${project.mc}")
            }

            semver = when (project.mc) {
                "1.16.5" -> "~1.16.2"
                "1.17.1" -> "~1.17"
                "1.18.2" -> "~1.18.2"
                "1.19.4" -> "~1.19.4"
                "1.20.6" -> "~1.20.5"
                "1.21.11" -> ">=1.21.11"
                else -> error("Unsupported MC version: ${project.mc}")
            }
        }
    }

    buildOnly("parchment", required = false) {
        group = "org.parchmentmc.data"
        name = "parchment-${when (project.mc) {
            "1.20.6" -> "1.20.6"
            "1.21.11" -> "1.21.11"
            else -> null
        }}"
        version = when (project.mc) {
            "1.20.6" -> "2024.06.16"
            "1.21.11" -> "2024.11.17"
            else -> null
        }
        extension = "zip"
    }

    runtime("fabric-loader") {
        group = "net.fabricmc"
        name = "fabric-loader"
        version = when (project.mc) {
            "1.16.5" -> "0.12.11"
            "1.17.1" -> "0.12.11"
            "1.18.2" -> "0.12.11"
            "1.19.4" -> "0.12.11"
            "1.20.6" -> "0.18.0"
            "1.21.11" -> "0.18.0"
            else -> error("Unhandled mcVersion for fabric-loader: ${project.mc}")
        }

        requires {
            semver = when (project.mc) {
                "1.16.5" -> ">=0.12.11"
                "1.17.1" -> ">=0.12.11"
                "1.18.2" -> ">=0.12.11"
                "1.19.4" -> ">=0.12.11"
                "1.20.6" -> ">=0.12.11"
                "1.21.11" -> ">=0.18.0"
                else -> error("Unhandled mcVersion for fabric-loader requirement: ${project.mc}")
            }

            maven = "" // FIXME
        }
    }

    published("fabric-api") {
        group = "net.fabricmc.fabric-api"
        name = "fabric-api"
        version = when (project.mc) {
            "1.16.5" -> "0.42.0+1.16"
            "1.17.1" -> "0.46.1+1.17"
            "1.18.2" -> "0.77.0+1.18.2"
            "1.19.4" -> "0.87.2+1.19.4"
            "1.20.6" -> "0.100.8+1.20.6"
            "1.21.11" -> "0.139.4+1.21.11"
            else -> error("Unhandled mcVersion for fabric-api: ${project.mc}")
        }

        // unbounded runtime requirement
        requires {
            maven = "(,)"
            semver = "*"
        }

        publishing {
            type = RelationshipType.REQUIRED
            curseforgeId = "fabric-api"
            modrinthId = "P7dR8mSH"
        }
    }

    published("modmenu") {
        group = "com.terraformersmc"
        name = "modmenu"
        version = when (project.mc) {
            "1.16.5" -> "1.16.23"
            "1.17.1" -> "2.0.17"
            "1.18.2" -> "3.2.5"
            "1.19.4" -> "6.3.1"
            "1.20.6" -> "10.0.0"
            "1.21.11" -> "17.0.0-beta.2"
            else -> error("Unhandled mcVersion for modmenu: ${project.mc}")
        }

        requires {
            // runtime requirement = fabric_mc_req from legacy data
            maven = when (project.mc) {
                "1.16.5" -> "[1.16.2,)"
                "1.17.1" -> "[1.17,)"
                "1.18.2" -> "[1.18.2,)"
                "1.19.4" -> "[1.19.4,)"
                "1.20.6" -> "[1.20.5,)"
                "1.21.11" -> "[1.21.11,)"
                else -> error("Unhandled mcVersion for modmenu runtime requirement: ${project.mc}")
            }

            semver = when (project.mc) {
                "1.16.5" -> "~1.16.2"
                "1.17.1" -> "~1.17"
                "1.18.2" -> "~1.18.2"
                "1.19.4" -> "~1.19.4"
                "1.20.6" -> "~1.20.5"
                "1.21.11" -> ">=1.21.11"
                else -> error("Unhandled mcVersion for modmenu semver: ${project.mc}")
            }
        }

        publishing {
            type = RelationshipType.OPTIONAL
            curseforgeId = "modmenu"
            modrinthId = "mOgUt4GM"
        }
    }

    published("cloth-config") {
        group = "me.shedaniel.cloth"
        name = when (project.loader) {
            "fabric" -> "cloth-config-fabric"
            "forge" -> "cloth-config-forge"
            "neoforge" -> "cloth-config-neoforge"
            else -> error("Unknown loader: ${project.loader}")
        }
        version = when (project.mc) {
            "1.16.5" -> "4.17.101"
            "1.17.1" -> "5.3.63"
            "1.18.2" -> "6.5.102"
            "1.19.4" -> "10.1.117"
            "1.20.6" -> "14.0.126"
            "1.21.11" -> "21.11.153"
            else -> error("Unhandled mcVersion for cloth-config: ${project.mc}")
        }

        requires {
            // runtime requirement: exact-or-newer
            // TODO: relax
            maven = "[$version,)"
            semver = "~$version"
        }

        publishing {
            // Forge pre-1.18 had no jar-jar support
            type = if (project.loader == "forge" && project.sc.current.parsed < "1.18") {
                RelationshipType.REQUIRED
            } else {
                RelationshipType.INCLUDED
            }

            curseforgeId = "cloth-config"
            modrinthId = "9s6osm5g"
        }
    }

    runtime("forge") {
        group = "net.minecraftforge"
        name = "forge"
        version = when (project.mc) {
            "1.17.1" -> "36.2.41"
            "1.18.2" -> "37.1.1"
            "1.19.4" -> "40.2.14"
            "1.20.6" -> "45.2.6"
            else -> error("Unhandled mcVersion for forge: ${project.mc}")
        }

        requires {
            maven = when (project.mc) {
                "1.17.1" -> "[34,)"
                "1.18.2" -> "[37,)"
                "1.19.4" -> "[40,)"
                "1.20.6" -> "[45,)"
                else -> error("Unhandled mcVersion for forge runtime requirement: ${project.mc}")
            }

            semver = "" // FIXME
        }
    }

    runtime("neoforge") {
        group = "net.neoforged"
        name = "neoforge"
        version = when (project.mc) {
            "1.20.6" -> "20.6.139"
            "1.21.11" -> "21.11.6-beta"
            else -> error("Unhandled mcVersion for neoforge: ${project.mc}")
        }

        requires {
            // runtime requirement = deps.neoforge_req
            maven = when (project.mc) {
                "1.20.6" -> "[20.6.11-beta,)"
                "1.21.11" -> "[21.9.0-beta,)"
                else -> error("Unhandled mcVersion for neoforge runtime requirement: ${project.mc}")
            }

            semver = "" // FIXME
        }
    }
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        loadStonecutterVersions().forEach { (name, mcVersions) ->
            branch(name) { versions(mcVersions) }
        }
    }
}

rootProject.name = "freecam"
