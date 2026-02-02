package net.xolt.freecam.serialization

import dev.eav.tomlkt.TomlDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.xolt.freecam.model.LibraryEntry
import net.xolt.freecam.model.ModLibraryDefinition
import net.xolt.freecam.model.ModLibrarySchema.LOADER_OVERRIDES
import net.xolt.freecam.model.ModLibrarySchema.MC_OVERRIDES

internal object ModLibraryDefinitionSerializer : KSerializer<ModLibraryDefinition> {

    private val delegate: KSerializer<Map<String, LibraryEntry>> by lazy {
        MapSerializer(String.serializer(), LibraryEntry.serializer())
    }

    private val overrideDescriptor = MapSerializer(String.serializer(), delegate).descriptor
    private val freeformDescriptor = MapSerializer(String.serializer(), String.serializer()).descriptor
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("ModLibraryDefinition") {
            element(MC_OVERRIDES,  overrideDescriptor, isOptional = true)
            element(LOADER_OVERRIDES, overrideDescriptor, isOptional = true)
            element("freeform", freeformDescriptor, isOptional = true)
        }

    override fun deserialize(decoder: Decoder): ModLibraryDefinition {
        if (decoder !is TomlDecoder) {
            throw SerializationException("Only Toml Decoder supported by ${this::class.simpleName}")
        }

        val table = decoder.decodeSerializableValue(delegate)

        val mc = (table[MC_OVERRIDES] as? LibraryEntry.Overrides)?.value ?: emptyMap()
        val loader = (table[LOADER_OVERRIDES] as? LibraryEntry.Overrides)?.value ?: emptyMap()

        val freeform =
            table
                .filterKeys { it != MC_OVERRIDES && it != LOADER_OVERRIDES }
                .mapValues { (it.value as LibraryEntry.Freeform).value }

        return ModLibraryDefinition(mc, loader, freeform)
    }

    override fun serialize(encoder: Encoder, value: ModLibraryDefinition) {
        val out: Map<String, LibraryEntry> = buildMap {
            if (value.mcOverrides.isNotEmpty()) {
                put(MC_OVERRIDES, LibraryEntry.Overrides(value.mcOverrides))
            }

            if (value.loaderOverrides.isNotEmpty()) {
                put(LOADER_OVERRIDES, LibraryEntry.Overrides(value.loaderOverrides))
            }

            value.freeform.forEach { (k, v) ->
                put(k, LibraryEntry.Freeform(v))
            }
        }

        encoder.encodeSerializableValue(delegate, out)
    }
}
