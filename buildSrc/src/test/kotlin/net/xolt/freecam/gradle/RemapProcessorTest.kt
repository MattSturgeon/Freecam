package net.xolt.freecam.gradle

import kotlin.test.Test
import kotlin.test.assertEquals

class RemapProcessorTest {

    @Test
    fun `Simple remap`() {
        val sample1 = mapOf(
            "some.key" to "Some value!",
        )
        val expected = mapOf(
            "remapped.key" to "Some value!",
        )
        val processor = RemapProcessor(
            "some.key" to "remapped.key"
        )

        val result = processor.process(sample1, null)

        assertEquals(expected, result)
    }

    @Test
    fun `Remap to multiple keys`() {
        val sample1 = mapOf(
            "some.key" to "Some value!",
        )
        val expected = mapOf(
            "remapped.key" to "Some value!",
            "other.key" to "Some value!",
        )
        val processor = RemapProcessor(
            "some.key" to "remapped.key",
            "some.key" to "other.key"
        )

        val result = processor.process(sample1, null)

        assertEquals(expected, result)
    }

    @Test
    fun `Empty remap removes key`() {
        val sample1 = mapOf(
            "some.key" to "Some value!",
        )
        val expected = mapOf<String, String>()
        val processor = RemapProcessor(mapOf(
            "some.key" to listOf()
        ))

        val result = processor.process(sample1, null)

        assertEquals(expected, result)
    }

    @Test
    fun `Does not modify unmapped keys`() {
        val sample1 = mapOf(
            "some.key" to "Some value!",
            "unmapped.key" to "Unmapped value!",
        )
        val expected = mapOf(
            "remapped.key" to "Some value!",
            "unmapped.key" to "Unmapped value!",
        )
        val processor = RemapProcessor(
            "some.key" to "remapped.key",
        )

        val result = processor.process(sample1, null)

        assertEquals(expected, result)
    }

    @Test
    fun `Does not promote fallback keys`() {
        val fallback = mapOf(
            "fallback.key" to "Fallback value!",
        )
        val sample1 = mapOf(
            "unmapped.key" to "Unmapped value!",
        )
        val expected = mapOf(
            "unmapped.key" to "Unmapped value!",
        )
        val processor = RemapProcessor(
            "fallback.key" to "remapped.key",
        )

        val result = processor.process(sample1, fallback)

        assertEquals(expected, result)
    }
}