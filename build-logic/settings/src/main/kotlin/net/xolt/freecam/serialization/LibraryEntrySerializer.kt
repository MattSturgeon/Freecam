package net.xolt.freecam.serialization

import dev.eav.tomlkt.TomlDecoder
import dev.eav.tomlkt.TomlLiteral
import dev.eav.tomlkt.TomlTable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.xolt.freecam.model.LibraryEntry
import net.xolt.freecam.model.ModLibraryOverrides

internal object LibraryEntrySerializer : KSerializer<LibraryEntry> {

    private val overrideSerializer: KSerializer<ModLibraryOverrides> by lazy {
        MapSerializer(String.serializer(), ModLibraryDefinitionSerializer)
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("LibraryEntry")

    override fun deserialize(decoder: Decoder): LibraryEntry {
        if (decoder !is TomlDecoder) {
            throw SerializationException("Only Toml Decoder supported by ${this::class.simpleName}")
        }

        return when (val element = decoder.decodeTomlElement()) {
            is TomlTable ->
                LibraryEntry.Overrides(decoder.toml.decodeFromTomlElement(overrideSerializer, element))

            is TomlLiteral ->
                LibraryEntry.Freeform(element.content)

            else ->
                throw SerializationException("Unsupported TOML element for LibraryEntry: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: LibraryEntry) {
        when (value) {
            is LibraryEntry.Overrides ->
                encoder.encodeSerializableValue(overrideSerializer, value.value)

            is LibraryEntry.Freeform ->
                encoder.encodeString(value.value)
        }
    }
}
