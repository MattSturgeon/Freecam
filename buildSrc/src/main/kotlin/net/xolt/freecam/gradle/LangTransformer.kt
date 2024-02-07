package net.xolt.freecam.gradle

/**
 * A transformer that modifies a translations map somehow.
 */
fun interface LangTransformer {
    /**
     * Transform the provided [translations] and return the result.
     */
    fun transform(
        translations: Map<String, String>,
        fallback: Map<String, String>?
    ): Map<String, String>
}
