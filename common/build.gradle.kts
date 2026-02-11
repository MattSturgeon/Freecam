import dev.kikugie.stonecutter.StonecutterExperimentalAPI

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.fletchingtable.fabric)
    id("freecam.common")
}

stonecutter {

}

fletchingTable {
    j52j.register("main") {
        extension("json", "**/*.json5")
    }
}

loom {
    // Loom reads the AW during configuration, so the :stonecutterGenerate one is too late
    // We still use the task-generated AW during the actual build
    accessWidenerPath = provider {
        @OptIn(StonecutterExperimentalAPI::class)
        stonecutter.process(
            file = rootDir.resolve("common/src/main/resources/freecam.accesswidener"),
            destination = "build/generated-eval/freecam.accesswidener"
        )
    }

    mixin {
        useLegacyMixinAp = false
    }
}

dependencies {
    "com.mojang:minecraft:${meta.mc}".also { old ->
        val new = modLibraries.minecraft.maven.coordinate
        require(old == new)
        minecraft(new)
    }

    mappings(loom.layered {
        officialMojangMappings()
        meta.deps.orNull("parchment").also { old ->
            val new = modLibraries.parchment
            require((old == null) == (new == null)) {
                "${project.path} old $old, new $new"
            }
        }
        meta.parchment { mappings, mc ->
            "org.parchmentmc.data:parchment-${mc}:$mappings@zip".also { old ->
                val new = modLibraries.parchment!!.maven.coordinate
                require(old == new) {
                    "${project.path} old $old, new $new"
                }
                parchment(new)
            }
        }
    })

    "org.spongepowered:mixin:${meta.deps["mixin"]}".also { old ->
        val new = modLibraries.mixin.maven.coordinate
        require(old == new) {
            "${project.path} old $old new $new"
        }
        compileOnly(new)
    }

    modCompileOnly("net.fabricmc:fabric-loader:${meta.deps["fabric_loader"]}")
    "net.fabricmc:fabric-loader:${meta.deps["fabric_loader"]}".also { old ->
        val new = modLibraries.fabricLoader.maven.coordinate
        require(old == new) {
            "${project.path} old $old new $new"
        }
        modImplementation(new)
    }

    "me.shedaniel.cloth:cloth-config-fabric:${meta.deps["cloth"]}".also { old ->
        val new = modLibraries.clothConfig.maven.coordinate
        require(old == new) {
            "${project.path} old $old, new $new"
        }
        modCompileOnly(new)
    }
}

tasks.processResources {
    filesMatching("freecam-common.mixins.json") {
        expand(commonJsonExpansions)
    }
}