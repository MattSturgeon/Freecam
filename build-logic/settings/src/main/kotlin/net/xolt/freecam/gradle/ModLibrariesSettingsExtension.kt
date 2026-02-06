package net.xolt.freecam.gradle

import net.xolt.freecam.asm.ModLibrariesAbiModel
import net.xolt.freecam.gradle.dsl.*
import net.xolt.freecam.model.BuildOnlyLibrary
import net.xolt.freecam.model.ModLibrary
import net.xolt.freecam.model.PublishedLibrary
import net.xolt.freecam.model.RuntimeLibrary
import org.gradle.api.Project
import kotlin.reflect.KClass

abstract class ModLibrariesSettingsExtension {

    fun buildOnly(id: String, required: Boolean = true, block: BuildOnlyLibrarySpec.() -> Unit) {
        buildOnlyDefs.computeIfAbsent(id) { mutableListOf() }
            .add(ModLibrarySpecDefinition(id, required, block))
    }

    fun runtime(id: String, required: Boolean = true, block: RuntimeLibrarySpec.() -> Unit) {
        runtimeDefs.computeIfAbsent(id) { mutableListOf() }
            .add(ModLibrarySpecDefinition(id, required, block))
    }

    fun published(id: String, required: Boolean = true, block: FullLibrarySpec.() -> Unit) {
        publishDefs.computeIfAbsent(id) { mutableListOf() }
            .add(ModLibrarySpecDefinition(id, required, block))
    }

    internal fun validatedLibrarySpecs() = librarySpecs.also {
        ModLibrariesAbiModel(it).validate()
    }

    private val buildOnlyDefs = mutableMapOf<String, MutableList<ModLibrarySpecDefinition<BuildOnlyLibrarySpec>>>()
    private val runtimeDefs = mutableMapOf<String, MutableList<ModLibrarySpecDefinition<RuntimeLibrarySpec>>>()
    private val publishDefs = mutableMapOf<String, MutableList<ModLibrarySpecDefinition<FullLibrarySpec>>>()

    private val librarySpecs get() = buildList {
        reduceDefs<BuildOnlyLibrarySpec, BuildOnlyLibrary>(buildOnlyDefs) { project, specs ->
            BuildOnlyLibrarySpecImpl(project).apply { specs.forEach(::apply) }
        }.also(::addAll)

        reduceDefs<RuntimeLibrarySpec, RuntimeLibrary>(runtimeDefs) { project, specs ->
            RuntimeLibrarySpecImpl(project).apply { specs.forEach(::apply) }
        }.also(::addAll)

        reduceDefs<FullLibrarySpec, PublishedLibrary>(publishDefs) { project, specs ->
            FullLibrarySpecImpl(project).apply { specs.forEach(::apply) }
        }.also(::addAll)
    }

    private inline fun <S : ModLibrarySpec<S>, reified T : ModLibrary> reduceDefs(
        definitions: Map<String, List<ModLibrarySpecDefinition<S>>>,
        noinline reduce: (Project, List<S.() -> Unit>) -> ModLibraryBuilder<T>,
    ): List<ModLibraryDefinition<T>> = definitions.map { (id, libDefs) ->
        val required = libDefs.first().required
        require(libDefs.all { it.required == required }) {
            "Conflicting `required` definitions for '$id'"
        }
        ModLibraryDefinition(
            id = id,
            required = required,
            outType = T::class,
            factory = { project ->
                reduce(project, libDefs.map { it.spec }).build()
            },
        )
    }
}

private data class ModLibrarySpecDefinition<T>(
    val id: String,
    val required: Boolean,
    val spec: T.() -> Unit,
)

internal data class ModLibraryDefinition<T : ModLibrary>(
    val id: String,
    val required: Boolean,
    val outType: KClass<T>,
    val factory: ModLibraryFactory<T>,
)

internal typealias ModLibraryFactory<T> = (Project) -> T?
