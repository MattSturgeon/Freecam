package net.xolt.freecam.model

import dev.eav.tomlkt.Toml
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import net.xolt.freecam.model.resolution.FieldDefinition
import net.xolt.freecam.model.resolution.FieldDefinitions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

internal object ModLibrarySchema {
    const val GROUP = "group"
    const val NAME = "name"
    const val VERSION = "version"
    const val RELATIONSHIP_TYPE = "relationship_type"
    const val CURSEFORGE_ID = "curseforge_id"
    const val MODRINTH_ID = "modrinth_id"
    const val SEMVER_REQ = "semver_requirement"
    const val MAVEN_REQ = "maven_requirement"
    const val LOADER_OVERRIDES = "loader"
    const val MC_OVERRIDES = "mc"

    val ALL = setOf(
        GROUP,
        NAME,
        VERSION,
        RELATIONSHIP_TYPE,
        CURSEFORGE_ID,
        MODRINTH_ID,
        SEMVER_REQ,
        MAVEN_REQ,
    )

    val REQUIRED = setOf(
        GROUP,
        NAME,
        VERSION,
    )

    /**
     * Maps the [library catalog][ModLibraries]'s property names to their serialized names.
     */
    val PROPERTY_NAMES by lazy {
        ModLibraries::class.memberProperties.associate { property ->
            property.name to (property.findAnnotation<SerialName>()?.value ?: property.name)
        }
    }

    /**
     * The set of all [library catalog][ModLibraries] serialized property names.
     * I.e., the names we expect to see in the [TOML file][ModLibrariesFile].
     */
    val SERIAL_NAMES: Set<String> by lazy {
        PROPERTY_NAMES.values.toSet()
    }
}

/**
 * Does final validation of a resolved [ModLibraryDefinition]'s fields, merging duplicate definitions
 * and throwing if conflicting, missing, or unexpected definitions are found.
 */
internal fun FieldDefinitions.resolveFields(id: String): ModLibraryFields {
    // Collect errors and defer throwing until we're done
    val errors = mutableListOf<String>()
    errors.addAll(collectMissingKeys())
    errors.addAll(collectUnknownKeys())

    val result = buildMap {
        for ((key, definitions) in this@resolveFields) {
            // We can merge equal values, but multiple distinct values is an error.
            val values = definitions.map(FieldDefinition::value).distinct()
            when {
                values.size > 1 -> {
                    val limit = 5
                    val context = definitions
                        .sortedBy { it.displayPath }
                        .joinToString(
                            separator = "\n",
                            limit = limit,
                            truncated = "  (${definitions.size - limit} values omitted)",
                        ) { "  - ${it.diagnosticString}" }
                    errors += "Conflicting values for field '$key':\n$context"
                }
                else -> put(key, values.single())
            }
        }
    }

    errors
        .takeIf { it.isNotEmpty() }
        ?.let { errors ->
            error("Invalid library definition '$id':${errors.joinToString(
                prefix = "\n",
                separator = "\n",
                transform = { "  - $it" },
            )}")
        }

    return result
}

internal fun FieldDefinitions.collectUnknownKeys(): List<String> =
    (keys - ModLibrarySchema.ALL).map { "Unknown field '$it'" }

internal fun FieldDefinitions.collectMissingKeys(): List<String> = buildList {
    addAll((ModLibrarySchema.REQUIRED - keys).map {
        "Missing required field '$it'"
    })

    val hasRelationshipType = containsKey("relationship_type")
    val hasRelationshipIds = containsKey("curseforge_id") || containsKey("modrinth_id")
    if (hasRelationshipIds && !hasRelationshipType) {
        add("relationship_type is required when curseforge_id or modrinth_id is defined")
    }
    if (hasRelationshipType && !hasRelationshipIds) {
        add("curseforge_id or modrinth_id is required when relationship_type is defined")
    }

    if (containsKey("semver_requirement") && !containsKey("maven_requirement")) {
        add("maven_requirement is required when semver_requirement is defined")
    }
    if (containsKey("maven_requirement") && !containsKey("semver_requirement")) {
        add("semver_requirement is required when maven_requirement is defined")
    }
}

internal fun Set<String>.assertExactMatch(keys: Set<String>) {
    val missing = this - keys
    val extra = keys - this

    require(missing.isEmpty() && extra.isEmpty()) {
        buildString {
            if (missing.isNotEmpty()) append("Missing libraries: $missing. ")
            if (extra.isNotEmpty()) append("Unknown libraries: $extra.")
        }
    }
}

private val BARE_TOML_KEY_REGEX = Regex("[A-Za-z0-9_-]+")

/**
 * Human-readable TOML-style representation of this field's key path.
 *
 * Uses dotted keys where possible and quoted keys when required.
 * Intended for diagnostics only; not a reversible or canonical TOML path.
 */
private val FieldDefinition.displayPath: String get() {
    if (path.isEmpty()) return "<root>"

    return path.joinToString(".") {
        if (BARE_TOML_KEY_REGEX.matches(it))
            it
        else
            Toml.encodeToString<String>(it)
    }
}

private val FieldDefinition.diagnosticString: String get() =
    "${displayPath}: ${Toml.encodeToString<String>(value)}"
