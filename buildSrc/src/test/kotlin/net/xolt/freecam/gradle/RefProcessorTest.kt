package net.xolt.freecam.gradle

import kotlin.test.Test
import kotlin.test.assertEquals

class RefProcessorTest {

    private val processor: LangProcessor = refProcessor

    @Test
    fun `Can inject other key`() {
        val sample1 = mapOf(
            "modid.description" to "Description, with {modid.summary}",
            "modid.summary" to "Summary!",
        )

        val expected = mapOf(
            "modid.description" to "Description, with Summary!",
            "modid.summary" to "Summary!",
        )

        val result = processor.process(sample1, null)

        assertEquals(expected, result)
    }

    @Test
    fun `Fails gracefully when key doesn't exist`() {
        val sample1 = mapOf(
            "modid.description" to "Description, with {dummy.key}",
            "modid.summary" to "Summary!",
        )

        val result = processor.process(sample1, null)

        assertEquals(sample1, result)
    }

    @Test
    fun `Uses fallback when key isn't translated`() {
        val fallback = mapOf(
            "untranslated.key" to "Fallback string!",
        )
        val sample1 = mapOf(
            "modid.description" to "Description, with {untranslated.key}",
        )
        val expected = mapOf(
            "modid.description" to "Description, with Fallback string!",
        )

        val result = processor.process(sample1, fallback)

        assertEquals(expected, result)
    }

    @Test
    fun `Uses fallback when template isn't translated`() {
        val fallback = mapOf(
            "untranslated.template" to "Description, with {some.key}",
            "some.key" to "Fallback value!",
        )
        val sample1 = mapOf(
            "some.key" to "Translated Value!",
        )
        val expected = mapOf(
            "untranslated.template" to "Description, with Translated Value!",
            "some.key" to "Translated Value!",
        )

        val result = processor.process(sample1, fallback)

        assertEquals(expected.toSortedMap(), result.toSortedMap())
    }

    @Test
    fun `Doesn't promote unrelated fallback strings`() {
        val fallback = mapOf(
            "untranslated.key" to "String, without templates",
        )
        val sample1 = mapOf(
            "some.key" to "Some Value!",
        )
        val expected = mapOf(
            "some.key" to "Some Value!",
        )

        val result = processor.process(sample1, fallback)

        assertEquals(expected, result)
    }
}