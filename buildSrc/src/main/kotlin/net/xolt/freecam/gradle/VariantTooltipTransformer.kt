package net.xolt.freecam.gradle

class VariantTooltipTransformer(private val variant: String) : LangTransformer {
    private val variantRegex = "\\.@(?<variant>[^.]+)Tooltip(?<index>\\[\\d+])?${'$'}".toRegex()

    override fun transform(
        transformed: Map<String, String>,
        translations: Translations,
    ): Map<String, String> {
        val map = transformed.toMutableMap()
        // Iterate over fallback values, to ensure variant-tooltips aren't accidentally overridden due to missing translations
//        fallback?.forEach { (key, _) ->
//            variantRegex.find(key)?.let { result ->
//                map.remove(key)
//                map.remove(baseKey(key, result))
//            }
//        }
        // Then overwrite with actual values
        transformed.forEach { (key, value) ->
            variantRegex.find(key)?.let { result ->
                // This is normally handled by the first loop, but fallback is nullable...
                map.remove(key)

                // Add the variant translation
                if (variant == result.groups["variant"]?.value?.lowercase()) {
                    map[baseKey(key, result)] = value
                }
            }
        }
        return map
    }

    private fun baseKey(variantKey: String, result: MatchResult): String {
        val index = result.groups["index"]?.value ?: ""
        return variantKey.replaceAfterLast('.', "@Tooltip${index}")
    }
}
