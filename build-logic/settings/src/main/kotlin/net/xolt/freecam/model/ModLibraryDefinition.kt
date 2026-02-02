package net.xolt.freecam.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.xolt.freecam.serialization.LibraryEntrySerializer
import net.xolt.freecam.serialization.ModLibraryDefinitionSerializer

@Serializable
internal data class ModLibrariesFile(
    val libraries: Map<String, ModLibraryDefinition>
)

@Serializable(with = ModLibraryDefinitionSerializer::class)
internal data class ModLibraryDefinition(
    @SerialName("mc")
    val mcOverrides: ModLibraryOverrides = emptyMap(),

    @SerialName("loader")
    val loaderOverrides: ModLibraryOverrides = emptyMap(),

    val freeform: ModLibraryFields = emptyMap(),
)

internal typealias ModLibraryFields = Map<String, String>
internal typealias ModLibraryOverrides = Map<String, ModLibraryDefinition>

@Serializable(with = LibraryEntrySerializer::class)
internal sealed interface LibraryEntry {
    data class Overrides(val value: ModLibraryOverrides) : LibraryEntry
    data class Freeform(val value: String) : LibraryEntry
}
