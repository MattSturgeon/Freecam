package net.xolt.freecam.gradle

import kotlinx.serialization.json.Json

class Translations(translations: Iterable<Pair<String, String>>, val variant: String, private val fallback: Translations? = null) {
    constructor(translations: Map<String, String>, variant: String, fallback: Translations? = null) : this(translations.map { it.toPair() }, variant, fallback)

    private val base: Map<String, String>
    private val variants: Map<String, String>

    init {
        base = buildMap {
            translations
                .asSequence()
                .map { (key, value) -> Key.fromKey(key) to value }
                .filter { it.first.variant == "" }
                .associateTo(this) { (first, second) ->
                    first.key to second
                }
        }
        variants = buildMap {
            if (variant != "") {
                translations
                    .asSequence()
                    .map { (key, value) -> Key.fromKey(key) to value }
                    .filter { it.first.variant == variant }
                    .associateTo(this) { (first, second) ->
                        first.key to second
                    }
            }
        }
    }

    fun get(key: String): String? = variants[key] ?: base[key]

    fun getWithFallback(key: String): String? = get(key) ?: fallback?.getWithFallback(key)

    fun hasVariant(key: String) = variants.containsKey(key) || base.containsKey(key)

    fun toMap() = buildMap {
        putAll(variants)
        base.asSequence()
            // Don't include translations that'd override variants
            .filterNot { (key, _) -> containsKey(key) }
            // Don't include translations that'd override fallback variants
            .filterNot { (key, _) -> fallback?.variants?.containsKey(key) ?: false }
            .associateTo(this) { it.toPair() }
        }

    companion object {
        fun fromJson(json: String, variant: String, fallback: Translations? = null): Translations {
            val map: Map<String, String> = Json.decodeFromString(json)
            return Translations(map.map { it.toPair() }, variant, fallback)
        }
    }
}

private data class Key(val key: String, val variant: String) {
    companion object {
        fun fromKey(key: String): Key {
            val lastDot = key.lastIndexOf('.')
            val lastColon = key.lastIndexOf(':')

            // No "variant" separator
            if (lastColon < 0) {
                return Key(key, "")
            }

            // "variant" separator isn't in final section
            if (lastColon < lastDot) {
                return Key(key, "")
            }

            val variant = key.drop(lastColon + 1)
            val base = key.substring(0, lastColon)
            return Key(base, variant)
        }
    }
}

