package net.xolt.freecam.gradle

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import kotlin.test.assertEquals

/**
 * Declares a test for [LangProcessor] behavior.
 */
internal fun processorTest(
    name: String,
    processor: LangProcessor,
    translations: Map<String, String>,
    fallback: Map<String, String>? = null,
    result: Map<String, String>,
): DynamicTest = dynamicTest(name) {
    assertEquals(result, processor.process(translations, fallback))
}