package net.xolt.freecam.model.resolution

import kotlinx.serialization.SerializationException
import net.xolt.freecam.model.*
import net.xolt.freecam.model.ModLibrarySchema.LOADER_OVERRIDES
import net.xolt.freecam.model.ModLibrarySchema.MC_OVERRIDES
import net.xolt.freecam.serialization.decodeAs

internal typealias FieldDefinitions = Map<String, List<FieldDefinition>>

internal data class LibraryDefinitionNode(
    val path: List<String>,
    val definition: ModLibraryDefinition,
)

internal data class FieldDefinition(
    val path: List<String>,
    val value: String,
)

/**
 * Recursively walks this definition and all applicable overrides for the given loader and Minecraft version,
 * producing a list of [LibraryDefinitionNode] annotated with their override path.
 *
 * Each node’s [LibraryDefinitionNode.path] represents its location in the override hierarchy:
 * - The root node has an empty path.
 * - MC overrides append `[MC_OVERRIDES, mcVersion]`.
 * - Loader overrides append `[LOADER_OVERRIDES, loader]`.
 *
 * Ordering of siblings (e.g., multiple overrides at the same level) is an implementation detail.
 * The output is not sorted; callers must not rely on iteration order and should group nodes by path-depth if needed.
 */
internal fun ModLibraryDefinition.selectDefinitionsRecursive(
    loader: String,
    mcVersion: String
): List<LibraryDefinitionNode> = buildList {
    fun ModLibraryDefinition.walk(path: List<String> = emptyList()) {
        add(LibraryDefinitionNode(path = path, definition = this))
        mcOverrides[mcVersion]?.walk(path + listOf(MC_OVERRIDES, mcVersion))
        loaderOverrides[loader]?.walk(path + listOf(LOADER_OVERRIDES, loader))
    }
    walk()
}

/**
 * Collects and flattens a [ModLibraryDefinition]'s table of Minecraft version and mod loader–specific
 * [overrides][ModLibraryOverrides].
 *
 * The result is reduced to a [FieldDefinitions] map, containing only the highest-priority definitions for each field.
 *
 * Override priority is determined by specificity: deeper nodes in the override hierarchy have higher priority.
 *
 * If a field is defined multiple times at the same priority level, all highest-priority definitions are retained. These
 * definitions retain their source paths for diagnostic purposes. This allows [resolveFields] to either merge compatible
 * values or raise a 'conflicting definitions' error.
 */
internal fun ModLibraryDefinition.selectHighestPriorityFields(loader: String, mcVersion: String): FieldDefinitions =
    selectDefinitionsRecursive(loader, mcVersion)
        .groupBy { it.path.size }
        .toSortedMap()
        .values
        .fold(emptyMap()) { acc, layer ->
            // Transpose the list of map into a map of list
            // Each value is a non-empty list, with non-singleton lists indicating a potential definition conflict
            // Non-empty fields are added to the accumulator, shadowing previous definitions
            acc + layer
                .flatMap { node ->
                    node.definition.freeform.map { (key, value) ->
                        key to FieldDefinition(value = value, path = node.path + key)
                    }
                }
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second },
                )
                .filterValues { it.isNotEmpty() }
        }

/**
 * Resolves a TOML [definition][ModLibraryDefinition] into a complete [ModLibrary] object, for use in the Gradle build.
 *
 * This applies loader and version specific overrides and validates that the final definition is internally consistent.
 */
internal fun ModLibraryDefinition.buildModLibrary(id: String, loader: String, mcVersion: String): ModLibrary {
    val merged = selectHighestPriorityFields(loader, mcVersion).resolveFields(id)

    val group = merged[ModLibrarySchema.GROUP] ?: error("Null group should be caught by validation")
    val name = merged[ModLibrarySchema.NAME] ?: error("Null name should be caught by validation")
    val version = merged[ModLibrarySchema.VERSION] ?: error("Null version should be caught by validation")

    val relationship = merged["relationship_type"]?.let { typeStr ->
        val type = try {
            typeStr.lowercase().decodeAs<ModRelationship.Type>()
        } catch (e: SerializationException) {
            val message = e.message?.let {
                it.removeSuffix("at path $") + "at field 'relationship_type'"
            }
            throw SerializationException(message, e.cause)
        }
        ModRelationship(
            type = type,
            curseforgeId = merged["curseforge_id"],
            modrinthId = merged["modrinth_id"]
        )
    }

    val requirement = ModVersionRange(
        semver = merged["semver_requirement"] ?: "",
        maven = merged["maven_requirement"] ?: "",
    ).takeIf {
        it.semver.isNotEmpty() || it.maven.isNotEmpty()
    }

    return ModLibrary(
        id = id,
        group = group,
        name = name,
        version = version,
        relationship = relationship,
        requirement = requirement
    )
}

/**
 * Resolves a TOML [mod libraries file][ModLibrariesFile] into a complete [ModLibraries] catalog,
 * for use in the Gradle build.
 *
 * Uses reflection to avoid maintaining the list of libraries we catalog in multiple places.
 */
internal fun ModLibrariesFile.buildCatalog(loader: String, mcVersion: String): ModLibraries {
    // Validate keys against the actual ModLibraries field names
    ModLibrarySchema.SERIAL_NAMES.assertExactMatch(libraries.keys)

    val klass = ModLibraries::class
    val ctor = klass.constructors.single()

    // Get ModLibrary configuration for each constructor parameter
    val args = ctor.parameters.associateWith { param ->
        requireNotNull(param.name) {
            "${klass.simpleName}: Unnamed constructor parameter"
        }
        val id = requireNotNull(ModLibrarySchema.PROPERTY_NAMES[param.name]) {
            "${klass.simpleName}: Constructor parameter ${param.name} has no property name"
        }
        libraries.getValue(id).buildModLibrary(id, loader, mcVersion)
    }

    return ctor.callBy(args)
}