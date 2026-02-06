package net.xolt.freecam.gradle

import net.xolt.freecam.asm.GeneratedModLibrariesAccessorClass
import net.xolt.freecam.asm.findOrLoadClass
import net.xolt.freecam.asm.generateModLibrariesAccessorClass
import net.xolt.freecam.asm.writeClassAsJar
import net.xolt.freecam.model.DefaultModLibraryRegistry
import net.xolt.freecam.model.ModLibrary
import net.xolt.freecam.model.ModLibraryRegistry
import org.gradle.api.Plugin
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import org.slf4j.LoggerFactory.getLogger
import java.net.URLClassLoader

private const val EXTENSION_NAME = "modLibraries"
private val logger = getLogger("freecam.modlibraries")

/**
 * Settings plugin that generates and registers the `ModLibraries` project extension.
 *
 * Responsibilities:
 * 1. Generate the JVM class with type-safe accessors using ASM.
 * 2. Write it as a jar under `.gradle/generated-mod-libraries/`.
 * 3. Load the class into the settings classloader and create a singleton instance.
 * 4. Register it as a project extension on each project for Kotlin DSL access.
 *
 * NOTE: ClassLoader manipulation is required to satisfy Gradle's project extension
 * system and Kotlin DSL compiler visibility. Avoid double-loading or separate [URLClassLoader]s.
 */
class ModLibrariesSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        // Register the settings extension, where libraries are configured
        val settingsExtension = settings.extensions.create<ModLibrariesSettingsExtension>(EXTENSION_NAME)

        settings.gradle.settingsEvaluated {
            // Load and validate the settings-DSL configuration
            val libraries = settingsExtension.validatedLibrarySpecs()

            if (libraries.isEmpty()) {
                logger.info("No mod libraries defined")
                return@settingsEvaluated
            }

            // Generate accessor class & jar
            val generated = generateModLibrariesAccessorClass(libraries)
            val extensionJar = generated.writeJar(settings.layout.rootDirectory)

            // Load the class so we can register its extension...
            //
            // It is important that we load the class onto the active settings-plugin's classloader,
            // rather than the `project` or `settings` classloader, or a standalone classloader.
            val extensionClass = generated.loadInto(ModLibrariesSettingsPlugin::class.java.classLoader)

            // Add the jar to the buildscript classpath and register a project extension on all projects
            registerProjectExtensions(extensionJar, extensionClass, libraries.associateBy { it.id })
        }
    }

    private fun Settings.registerProjectExtensions(
        extensionJar: FileCollection,
        extensionClass: Class<*>,
        librarySpecs: Map<String, ModLibraryDefinition<out ModLibrary>>
    ) {
        gradle.beforeProject {
            // Make generated class visible to Kotlin DSL
            buildscript.dependencies.add("classpath", extensionJar)

            // Create a ModLibrariesExtension instance
            val extension = extensionClass
                .getConstructor(ModLibraryRegistry::class.java)
                .newInstance(DefaultModLibraryRegistry(project, librarySpecs))

            // Register generated project extension
            project.extensions.add(EXTENSION_NAME, extension)
        }
    }
}

private fun GeneratedModLibrariesAccessorClass.writeJar(rootDir: Directory): FileCollection {
    val file = rootDir
        .file(".gradle/generated-mod-libraries/dsl-accessors-$fingerprint.jar")
        .asFile

    when {
        file.exists() -> {
            // File is already up-to-date
            logger.debug("Generated {} already exists: {}", name, file.relativeTo(rootDir.asFile))
        }
        else -> {
            logger.info("Generating {}", name)
            file.parentFile.mkdirs()
            file.writeClassAsJar(name, bytes)
            logger.debug("{} written to: {}", name, file.relativeTo(rootDir.asFile))
        }
    }

    return rootDir.files(file)
}

private fun GeneratedModLibrariesAccessorClass.loadInto(classLoader: ClassLoader): Class<*> =
    classLoader.findOrLoadClass(name) { bytes }
