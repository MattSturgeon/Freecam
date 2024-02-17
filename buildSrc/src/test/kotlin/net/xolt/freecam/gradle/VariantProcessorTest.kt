package net.xolt.freecam.gradle

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VariantProcessorTest {

    private lateinit var processor: LangProcessor

    @BeforeTest
    fun setup() {
        processor = VariantProcessor("special")
    }

    @Test
    fun `Replaces base key with variant`() {
        val sample1 = mapOf(
            "modid.thing" to "Basic thing",
            "modid.thing:special" to "Special thing",
        )

        val expected = mapOf(
            "modid.thing" to "Special thing",
        )

        assertEquals(expected, processor.process(sample1, null))
    }

    @Test
    fun `Handles variant with no base key`() {
        val sample1 = mapOf(
            "modid.thing:special" to "Special thing",
        )

        val expected = mapOf(
            "modid.thing" to "Special thing",
        )

        assertEquals(expected, processor.process(sample1, null))
    }

    @Test
    fun `Do nothing when variant doesn't exist`() {
        val sample1 = mapOf(
            "modid.thing" to "Basic thing",
        )

        assertEquals(sample1, processor.process(sample1, null))
    }

    @Test
    fun `Uses fallback when variant isn't translated`() {
        val fallback = mapOf(
            "modid.thing:special" to "Special thing",
        )
        val sample1 = mapOf(
            "modid.thing" to "Basic thing",
        )
        val expected = mapOf(
            "modid.thing" to "Special thing",
        )

        assertEquals(expected, processor.process(sample1, fallback))
    }

    @Test
    fun `Uses fallback variant when nothing is translated`() {
        val fallback = mapOf(
            "modid.thing:special" to "Special thing",
        )
        val sample1 = mapOf<String, String>()
        val expected = mapOf(
            "modid.thing" to "Special thing",
        )

        assertEquals(expected, processor.process(sample1, fallback))
    }

    @Test
    fun `Removes variant keys`() {
        val sample1 = mapOf(
            "modid.thing" to "Basic thing",
            "modid.thing:special" to "Special thing",
            "modid.thing:other" to "Other thing",
        )
        val expected = mapOf(
            "modid.thing" to "Special thing",
        )

        assertEquals(expected, processor.process(sample1, null))
    }
}