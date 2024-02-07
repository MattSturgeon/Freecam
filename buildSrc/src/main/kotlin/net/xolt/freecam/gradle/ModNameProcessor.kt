package net.xolt.freecam.gradle

internal class ModNameProcessor(private val modID: String, private val variant: String) : LangProcessor {
    override fun process(
        translations: Map<String, String>,
        fallback: Map<String, String>?
    ): Map<String, String> {
        val firstID = "${modID}.name"
        val secondID = "${modID}.name.${variant}"
        val ids = listOf(firstID, secondID)

        // Nothing to do if this language has no "name" translations
        if (ids.none(translations.keys::contains)) {
            return translations
        }

        val map = translations.toMutableMap()

        // Remove any name.variant keys
        map.keys
            .filter { it.startsWith("${firstID}.") }
            .forEach(map::remove)

        // Set "full" name
        // Use fallback if either part is missing from this language
        ids.mapNotNull { translations[it] ?: fallback?.get(it) }
            .joinToString(" ")
            .let { name ->
                map[firstID] = name
                map["modmenu.nameTranslation.${modID}"] = name
            }

        return map
    }
}
