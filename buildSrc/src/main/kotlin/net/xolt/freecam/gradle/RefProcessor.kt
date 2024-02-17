package net.xolt.freecam.gradle

private val fmtRegex = """\{(?<ref>.+?)}""".toRegex()

internal val refProcessor = LangProcessor { translations, fallback ->
    // We use this mapper twice
    val transform: (Map.Entry<String, String>) -> Pair<String, String> = { (key, value) ->
        key to value.replace(fmtRegex) { match ->
            match.groups["ref"]?.value
                ?.let { ref ->
                    // Replace format string with referenced value
                    translations[ref] ?: fallback?.get(ref)
                }
                // Don't replace if ref wasn't found
                ?: match.value
        }
    }

    val mapped = translations.asSequence().associate(transform)

    val mappedFallback = fallback?.asSequence()
        ?.filterNot { mapped.containsKey(it.key) }
        ?.filter { fmtRegex.containsMatchIn(it.value) }
        ?.associate(transform)
        ?: emptyMap()

    mapped + mappedFallback
}