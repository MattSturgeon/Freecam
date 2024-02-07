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
import javax.inject.Inject

/**
 * A Gradle task that builds translations into minecraft lang files.
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
     * The "source" language that translations are based on.
     *
     * Defaults to `"en-US"`.
     */
    @get:Input
    abstract val source: Property<String>

    /**
     * Custom filename provider.
     *
     * A filename provider takes a lang string and returns a filename (or relative path).
     *
     * The provided filename will be resolved relative to [outputDirectory].
     *
     * Defaults to `"$it.json"`.
     *
     * Note: While any type can be returned, it's [toString] value will be used.
     * This is intended to support string-like types such as Groovy's `GString`.
     */
    @field:Inject
    var filenameProvider: (String) -> Any = { "$it.json" }

    private val json = Json { prettyPrint = true }
    private val localeRegex = "^[a-z]{2}-[A-Z]{2}$".toRegex()
    private val transformers = mutableListOf<LangTransformer>()

    init {
        @Suppress("LeakingThis")
        source.convention("en-US")
    }

    /**
     * Transform languages using the provided [function][LangTransformer].
     */
    fun transformer(transformer: LangTransformer) {
        transformers += transformer
    }

    /**
     * Run by Gradle when executing implementing tasks.
     */
    @TaskAction
    fun build() {
        val languages = inputDirectory.get().asFile
            .childDirectories()
            .filter { it.name.matches(localeRegex) }
            .associate { it.name to readLangDir(it) }
            .toMutableMap()

        // Handle the "source" language separately
        val base = languages.remove(source.get())?.let {
            buildLang(source.get(), it)
        }

        // Handle the "translation" languages
        languages.forEach { (lang, translations) ->
            buildLang(lang, translations, base)
        }
    }

    // Applies all transformers to the given translations.
    // Writes the result to the output file.
    // Does not use fallback to add missing translations, that is done in-game by MC
    // Some transformers may use fallback to fill in missing _parts_ of translations though.
    // Returns the built translations
    private fun buildLang(
        lang: String,
        translations: Map<String, String>,
        fallback: Map<String, String>? = null
    ): Map<String, String> {
        val built = transformers
            .fold(translations) { acc, transformer -> transformer.transform(acc, fallback) }
            .toSortedMap()
        writeJsonFile(fileFor(lang), built)
        return built
    }

    /**
     * Get the given translation, for the given language.
     *
     * Will fall back to using the [source language][source] if the key isn't
     * found in the specified language or if language isn't specified.
     *
     * Should only be used **after** this task has finished executing.
     * I.e. **not** during Gradle's configuration step.
     *
     * @param key the translation key
     * @param lang the locale, e.g. en-US, en_us, or zh-CN
     * @return the translation, or null if not found
     */
    @JvmOverloads
    fun getTranslation(key: String, lang: String = source.get()): String? {
        val file = fileFor(lang)
        val translation = readJsonFile(file)[key]

        // Check "source" translation if key wasn't found
        return if (translation == null && file != fileFor(source.get())) {
            getTranslation(key)
        } else {
            translation
        }
    }

    // Get the output file for a given lang, using filenameProvider.
    private fun fileFor(lang: String) = outputDirectory.get().asFile.resolve(filenameProvider(lang).toString())

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
