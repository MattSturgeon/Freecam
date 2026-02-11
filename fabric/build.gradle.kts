import dev.kikugie.stonecutter.StonecutterExperimentalAPI

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.fletchingtable.fabric)
    id("freecam.loaders")
}

stonecutter {

}

// TODO: debugging
println(modLibraries.fabricApi)

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
    "net.fabricmc:fabric-loader:${meta.deps["fabric_loader"]}".also { old ->
        val new = modLibraries.fabricLoader.maven.coordinate
        require(old == new) {
            "${project.path} old $old new $new"
        }
        modImplementation(new)
    }
    "net.fabricmc.fabric-api:fabric-api:${meta.deps["fabric_api"]}".also { old ->
        val new = modLibraries.fabricApi.maven.coordinate
        require(old == new) {
            "${project.path} old $old, new $new"
        }
        modApi(new) {
            exclude(module = "fabric-loader")
        }
    }

    "com.terraformersmc:modmenu:${meta.deps["modmenu"]}".also { old ->
        val new = modLibraries.modmenu.maven.coordinate
        require(old == new) {
            "${project.path} old $old, new $new"
        }
        modImplementation(new) {
            exclude(module = "fabric-api")
            exclude(module = "fabric-loader")
        }
    }

    "me.shedaniel.cloth:cloth-config-fabric:${meta.deps["cloth"]}".also { old ->
        val new = modLibraries.clothConfig.maven.coordinate
        require(old == new) {
            "${project.path} old $old, new $new"
        }
        modApi(new) {
            exclude(module = "fabric-api")
            exclude(module = "fabric-loader")
        }
        include(new)
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

    runs {
        getByName("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
        }
        getByName("server") {
//            server()
//            configName = "Fabric Server"
//            ideConfigGenerated(true)
            ideConfigGenerated(false)
        }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${meta.version}"))
    dependsOn(tasks.build)
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(commonJsonExpansions)
        }

        filesMatching("freecam-fabric.mixins.json") {
            expand(commonJsonExpansions)
        }

        inputs.properties(commonExpansions)
    }
}

publisher {
    artifact.set(tasks.named("remapJar"))

    listOf(curseDepends, modrinthDepends).forEach {
        it.required("fabric-api")
        it.optional("modmenu")
    }
}
