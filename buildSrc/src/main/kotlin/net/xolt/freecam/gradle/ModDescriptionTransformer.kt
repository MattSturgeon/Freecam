package net.xolt.freecam.gradle

internal class ModDescriptionTransformer(private val modID: String, private val variant: String) : LangTransformer {
    override fun transform(
        transformed: Map<String, String>,
        translations: Translations,
    ): Map<String, String> {
        val firstID = "${modID}.description"
        val secondID = "${modID}.description.suffix"
        val ids = listOf(firstID, secondID)

        // Nothing to do if this language has no "description" translations
//        if (ids.none { translations.hasVariant(it) }) {
//            return translations
//        }

        val map = transformed.toMutableMap()

        // Remove any description.variant keys
//        map.keys
//            .filter { it.startsWith("${firstID}.") }
//            .forEach(map::remove)
//
//        // Set modmenu summary if this language has a translation for firstID
//        translations[firstID]?.let { map["modmenu.summaryTranslation.${modID}"] = it }
//
//        // Set "full" description
//        // Use fallback if either part is missing from this language
//        ids.mapNotNull { translations[it] ?: fallback?.get(it) }
//            .joinToString(" ")
//            .let { description ->
//                map[firstID] = description
//                map["modmenu.descriptionTranslation.${modID}"] = description
//            }

        return map
    }
}
