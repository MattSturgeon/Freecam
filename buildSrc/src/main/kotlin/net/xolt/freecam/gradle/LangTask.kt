package net.xolt.freecam.gradle

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.xolt.freecam.extensions.childDirectories
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * A Gradle task that builds translations into [variant]-specific lang files, compatible with minecraft.
 */
abstract class LangTask : DefaultTask() {

    /**
     * The directory where language files should be loaded from.
     */
    @get:InputDirectory
    abstract val inputDirectory: DirectoryProperty

    /**
     * The directory where language files should be written to.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * The "build variant" of the language files to target.
     *
     * E.g. `"normal"` or `"modrinth"`.
     *
     * @sample "normal"
     * @sample "modrinth"
     */
    @get:Input
    abstract val variant: Property<String>

    /**
     * The "source" language that translations are based on.
     *
     * Defaults to `"en-US"`.
     */
    @get:Input
    abstract val source: Property<String>

    /**
     * The mod ID.
     *
     * Used in the output file structure as well as some translation keys.
     */
    @get:Input
    abstract val modId: Property<String>

    private val json = Json { prettyPrint = true }
    private val localeRegex = "^[a-z]{2}-[A-Z]{2}$".toRegex()

    init {
        source.convention("en-US")
    }

    /**
     * Run by Gradle when executing implementing tasks.
     */
    @TaskAction
    fun build() {
        val processors = listOf<LangProcessor>()

        val languages = inputDirectory.get().asFile
            .childDirectories()
            .filter { it.name.matches(localeRegex) }
            .associate { it.name to readLangDir(it) }

        val base = languages[source.get()]

        // Applies all processors to the given translations.
        // Does not use fallback to add missing translations, that is done in-game by MC
        // Some processors may use fallback to fill in missing _parts_ of translations though.
        languages.forEach { (lang, translations) ->
            writeJsonFile(fileFor(lang), processors.fold(translations) { acc, processor ->
                processor.process(acc, base)
            }.toSortedMap())
        }
    }

    private fun fileFor(lang: String) = outputDirectory.get().asFile
        .resolve("assets")
        .resolve(modId.get())
        .resolve("lang")
        .resolve(normaliseMCLangCode(lang) + ".json")

    // NOTE: Some lang codes may need manual mapping...
    // I couldn't find any examples though, so it's unlikely to affect us
    private fun normaliseMCLangCode(lang: String) = lang.lowercase().replace('-', '_')

    // Read and combine translation files in dir
    private fun readLangDir(dir: File) = dir
        .listFiles { _, name -> name.endsWith(".json") }
        ?.map { readJsonFile(it) }
        ?.flatMap { it.entries }
        ?.associate { it.toPair() }
        ?: emptyMap()

    @OptIn(ExperimentalSerializationApi::class)
    private fun readJsonFile(file: File): Map<String, String> = json.decodeFromStream(file.inputStream())

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeJsonFile(file: File, translations: Map<String, String>) {
        file.parentFile.mkdirs()
        file.createNewFile()
        json.encodeToStream(translations, file.outputStream())
    }
}