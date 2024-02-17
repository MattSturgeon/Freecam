package net.xolt.freecam.gradle

internal class VariantProcessor(private val variant: String) : LangProcessor {
    override fun process(
        translations: Map<String, String>,
        fallback: Map<String, String>?
    ): Map<String, String> {
        val map = mutableMapOf<String, String>()

        translations.forEach { (key, value) ->
            when (key.getVariant()) {
                "" -> {
                    // non-variant
                    // check fallback for variants
                    map.putIfAbsent(key, fallback?.get("$key:$variant") ?: value)
                }
                variant -> {
                    // matching variant
                    map[key.substringBeforeLast(':')] = value
                }
            }
        }

        // If a variant & its base key are not translated, it'll still be missing
        fallback
            ?.filterKeys { it.getVariant() == variant }
            ?.forEach { (key, value) ->
                map.putIfAbsent(key.substringBeforeLast(':'), value)
            }

        return map
    }

    private fun getVariant(
        key: String,
        translations: Map<String, String>,
        fallback: Map<String, String>?
    ): String? {
        return "$key:$variant".let {
            translations[it] ?: fallback?.get(it)
        }
    }
}

private fun String.getVariant() = substringAfterLast('.').substringAfterLast(':', "")

private fun String.isVariantKey(): Boolean {
    return substringAfterLast('.').contains(':')
}

