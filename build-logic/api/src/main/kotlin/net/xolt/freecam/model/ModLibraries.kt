package net.xolt.freecam.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ModLibraries(
    @SerialName("fabric_api")
    val fabricApi: ModLibrary,
    @SerialName("fabric_loader")
    val fabricLoader: ModLibrary,
    val cloth: ModLibrary,
    val modmenu: ModLibrary,
    val mixin: ModLibrary,
    val forge: ModLibrary,
    val neoforge: ModLibrary,
)

data class ModLibrary(
    val id: String,

    // Maven identity (resolved)
    val group: String,
    val name: String,
    val version: String,

    val relationship: ModRelationship? = null,
    val requirement: ModVersionRange? = null,
) {
    val coordinate: String
        get() = "$group:$name:$version"
}

/**
 * User-facing mod "relationship" data, shown on distribution sites like Modrinth.
 */
data class ModRelationship(
    val type: Type,
    val curseforgeId: String? = null,
    val modrinthId: String? = null,
) {
    @Serializable
    enum class Type {
        @SerialName("required")
        REQUIRED,
        @SerialName("optional")
        OPTIONAL,
        @SerialName("included")
        INCLUDED,
    }
}

/**
 * A user-facing version requirement, included in mod metadata files like `fabric.mod.json`.
 */
data class ModVersionRange(
    val semver: String,
    val maven: String,
)
