package net.xolt.freecam.gradle

import dev.eav.tomlkt.Toml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.xolt.freecam.model.ModMetadata
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.add
import java.io.File

@Serializable
private data class Metadata(
    val mod: ModMetadata,
)

class ModMetadataSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val metadata = loadMetadata(
            file = settings.rootDir.resolve("metadata.toml")
        )

        settings.extensions.add<BaseFreecamModExtension>("mod", SettingsModExtension(metadata.mod))

        settings.gradle.settingsEvaluated {
            gradle.allprojects {
                val extension: FreecamModExtension = ProjectModExtension(this, metadata.mod)
                extensions.add("mod", extension)
            }
        }
    }

    private fun loadMetadata(file: File): Metadata = Toml.decodeFromString<Metadata>(file.readText())
}
