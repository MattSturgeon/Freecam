package net.xolt.freecam.gradle

import org.junit.jupiter.api.DynamicTest
import kotlin.test.assertEquals

/**
 * Declares a test for [LangTransformer] behavior.
 */
internal fun processorTest(
    name: String,
    processor: LangTransformer,
    translations: Map<String, String>,
    fallback: Map<String, String>? = null,
    result: Map<String, String>,
): DynamicTest {
    // FIXME variant
    val f = fallback?.let { Translations(it, "") }
    val t = Translations(translations, "", f)
    return DynamicTest.dynamicTest(name) {
        assertEquals(result, processor.transform(t.toMap(), t))
    }
}
