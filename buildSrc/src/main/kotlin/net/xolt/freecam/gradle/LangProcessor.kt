package net.xolt.freecam.gradle

internal fun interface LangProcessor {
    fun process(
        translations: Map<String, String>,
        fallback: Map<String, String>?
    ): Map<String, String>
}
