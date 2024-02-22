package net.xolt.freecam.gradle

import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationsTest {

    @Test
    fun `Basic test`() {
        val data = listOf(
            "some.key" to "some value",
            "other.key" to "other value",
            "other.key:special" to "special value",
        )

        assertEquals("some value", Translations(data, "").get("some.key"))
        assertEquals("some value", Translations(data, "bad_variant").getWithFallback("some.key"))
        assertEquals("other value", Translations(data, "bad_variant").getWithFallback("other.key"))
        assertEquals("other value", Translations(data, "").getWithFallback("other.key"))
        assertEquals("special value", Translations(data, "special").getWithFallback("other.key"))
    }

    @Test
    fun `JSON test`() {
        @Language("JSON") val data = """
            {
              "some.key": "some value",
              "other.key": "other value",
              "other.key:special": "special value"
            }
        """.trimIndent()

        assertEquals("some value", Translations.fromJson(data, "").get("some.key"))
        assertEquals("some value", Translations.fromJson(data, "bad_variant").getWithFallback("some.key"))
        assertEquals("other value", Translations.fromJson(data, "bad_variant").getWithFallback("other.key"))
        assertEquals("other value", Translations.fromJson(data, "").getWithFallback("other.key"))
        assertEquals("special value", Translations.fromJson(data, "special").getWithFallback("other.key"))
    }

    @Test
    fun `Basic toMap test`() {
        @Language("JSON") val data = """
            {
              "some.key": "some value",
              "other.key": "other value",
              "other.key:special": "special value"
            }
        """.trimIndent()

        val normal = mapOf(
            "some.key" to "some value",
            "other.key" to "other value",
        )

        val special = mapOf(
            "some.key" to "some value",
            "other.key" to "special value",
        )

        assertEquals(normal, Translations.fromJson(data, "").toMap())
        assertEquals(special, Translations.fromJson(data, "special").toMap())
    }

    @Test
    fun `Fallback toMap test`() {
        @Language("JSON") val fallbackData = """
            {
              "some.key": "fallback value",
              "other.key": "other fallback value",
              "other.key:special": "special value"
            }
        """.trimIndent()

        @Language("JSON") val data = """
            {
              "some.key": "some value",
              "other.key": "other value"
            }
        """.trimIndent()

        val normal = mapOf(
            "some.key" to "some value",
            "other.key" to "other value",
        )

        // "other.key" should be omitted because it would override fallback's "special" variant
        val special = mapOf(
            "some.key" to "some value",
        )

        assertEquals(normal, Translations.fromJson(data, "", Translations.fromJson(fallbackData, "")).toMap())
        assertEquals(special, Translations.fromJson(data, "special", Translations.fromJson(fallbackData, "special")).toMap())
    }
}