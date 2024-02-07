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
    return DynamicTest.dynamicTest(name) {
        assertEquals(result, processor.transform(translations, fallback))
    }
}
