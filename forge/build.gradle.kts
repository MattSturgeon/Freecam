plugins {
    alias(libs.plugins.moddev.legacy)
    alias(libs.plugins.fletchingtable)
    id("freecam.loaders")
    id("freecam.atremapper")
}

fletchingTable {
    j52j.register("main") {
        extension("json", "**/*.json5")
    }

    accessConverter.register("main") {
        // During processResources, Fletching Table will exclude this file
        // and generate a META-INF/accesstransformer.cfg from it
        add("freecam.accesswidener")
    }
}

legacyForge {
    enable {
        meta.deps["forge"].also { old ->
            val new = modLibraries.forge.maven.version
            require(old == new) {
                "${project.path} old $old new $new"
            }
            forgeVersion = "${meta.mc}-$new"
        }
    }
}

dependencies {
    // TODO: move to version catalog
    compileOnlyApi("org.jetbrains:annotations:26.0.2")
    // TODO: move to version catalog
    annotationProcessor("org.spongepowered:mixin:${meta.deps["mixin"]}:processor")

    "me.shedaniel.cloth:cloth-config-forge:${meta.deps["cloth"]}".also { old ->
        val dep = modLibraries.clothConfig.maven
        val new = dep.coordinate
        require(old == new) {
            "${project.path} old $old new $new"
        }
        compileOnly(new)
        modRuntimeOnly(new)
        jarJar(modImplementation(group = dep.group, name = dep.name, version = dep.version))
    }
}

legacyForge {
    // Use the SRG-mapped accesstransformer
    accessTransformers.from(tasks.remapAtToSrg.map { it.outputs.files.singleFile })
    validateAccessTransformers = true

    runs {
        register("client") {
            client()
            ideName = "Forge Client (${project.path})"
        }
//        register("server") {
//            server()
//            ideName = "Forge Server (${project.path})"
//        }
    }

    require((modLibraries.parchment == null) == (meta.deps.orNull("parchment") == null))
    modLibraries.parchment?.also {
        parchment {
            minecraftVersion = it.maven.name.substringAfter('-')
            mappingsVersion = it.maven.version
            meta.parchment { mappings, mc ->
                require(mc == it.maven.name.substringAfter('-'))
                require(mappings == it.maven.version)
            }
        }
    }

    mods {
        register(meta.id) {
            sourceSet(sourceSets.main.get())
        }
    }
}

mixin {
    add(sourceSets.main.get(), "mixins.freecam.refmap.json")
    config("freecam-common.mixins.json")
    config("freecam-forge.mixins.json")
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.jar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${meta.version}"))
    dependsOn("build")
}

tasks.processResources {
    filesMatching(listOf("META-INF/mods.toml", "META-INF/forge.mods.toml")) {
        expand(commonExpansions)
    }

    filesMatching("freecam-forge.mixins.json") {
        expand(commonJsonExpansions)
    }

    filesMatching("pack.mcmeta") {
        expand(commonJsonExpansions)
    }

    inputs.properties(commonExpansions)
}

tasks.jar {
    finalizedBy("reobfJar")
}

publisher {
    artifact.set(tasks.named("jar"))
}
