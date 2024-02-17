package net.xolt.freecam.gradle

internal class RemapProcessor(private val map: Map<String, Iterable<String>>) : LangProcessor {
    constructor(vararg pairs: Pair<String, String>) : this(pairs.asIterable())
    constructor(pairs: Iterable<Pair<String, String>>) : this(pairs.groupBy({ it.first }, { it.second }))

    override fun process(
        translations: Map<String, String>,
        fallback: Map<String, String>?
    ): Map<String, String> {
        return translations.asSequence()
            .flatMap { (key, value) ->
                // Associate value with remap keys
                map[key]?.map { it to value }
                    // Otherwise use the existing key
                    ?: listOf(key to value)
            }
            .toMap()
    }
}