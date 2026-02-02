package net.xolt.freecam.model

import io.kotest.matchers.string.shouldContain
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ModLibraryKeyValidationTest {

    @Test
    fun `assertExactLibraries passes when sets match exactly`() {
        val required = setOf("a", "b", "c")
        val actual = setOf("a", "b", "c")

        required.assertExactMatch(actual) // should not throw
    }

    @Test
    fun `assertExactLibraries reports missing keys`() {
        val required = setOf("a", "b", "c")
        val actual = setOf("a", "b")

        val ex = assertFailsWith<IllegalArgumentException> {
            required.assertExactMatch(actual)
        }

        ex.message shouldContain "Missing libraries: [c]"
    }

    @Test
    fun `assertExactLibraries reports unknown keys`() {
        val required = setOf("a", "b")
        val actual = setOf("a", "b", "x")

        val ex = assertFailsWith<IllegalArgumentException> {
            required.assertExactMatch(actual)
        }

        ex.message shouldContain "Unknown libraries: [x]"
    }

    @Test
    fun `assertExactLibraries reports both missing and unknown keys`() {
        val required = setOf("a", "b", "c")
        val actual = setOf("a", "x")

        val ex = assertFailsWith<IllegalArgumentException> {
            required.assertExactMatch(actual)
        }

        ex.message shouldContain "Missing libraries: [b, c]"
        ex.message shouldContain "Unknown libraries: [x]"
    }
}
