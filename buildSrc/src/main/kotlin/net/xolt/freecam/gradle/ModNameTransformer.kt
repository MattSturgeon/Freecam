package net.xolt.freecam.gradle

internal class ModNameTransformer(private val modID: String, private val variant: String) : LangTransformer {
    override fun transform(
        transformed: Map<String, String>,
        translations: Translations,
    ): Map<String, String> {
        val firstID = "${modID}.name"
        val secondID = "${modID}.name.${variant}"
        val ids = listOf(firstID, secondID)

        // Nothing to do if this language has no "name" translations
//        if (ids.none(translations.keys::contains)) {
//            return translations
//        }

        val map = transformed.toMutableMap()

        // Remove any name.variant keys
//        map.keys
//            .filter { it.startsWith("${firstID}.") }
//            .forEach(map::remove)
//
//        // Set "full" name
//        // Use fallback if either part is missing from this language
//        ids.mapNotNull { translations[it] ?: fallback?.get(it) }
//            .joinToString(" ")
//            .let { name ->
//                map[firstID] = name
//                map["modmenu.nameTranslation.${modID}"] = name
//            }

        return map
    }
}
