package net.xolt.freecam.asm

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JavaIdentifierTest {

    @Test
    fun `result is always a valid Java identifier`() {
        val inputs = listOf(
            "simple",
            "123numericPrefix",
            "with-dash",
            "with space",
            "with.dot",
            "with/slash",
            "Ünicode",
            "",
        )

        inputs.forEach { input ->
            val id = input.toJavaIdentifier()
            when {
                input.isEmpty() -> id shouldBe input
                else -> {
                    id.first().isJavaIdentifierStart().shouldBeTrue()
                    id.all { it.isJavaIdentifierPart() }.shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `invalid characters are replaced with underscore`() {
        "foo-bar".toJavaIdentifier() shouldBe "foo_bar"
        "foo bar".toJavaIdentifier() shouldBe "foo_bar"
        "foo.bar".toJavaIdentifier() shouldBe "foo_bar"
    }

    @Test
    fun `invalid starting character is replaced`() {
        "1abc".toJavaIdentifier() shouldBe "_abc"
        "-abc".toJavaIdentifier() shouldBe "_abc"
    }

    @Test
    fun `already valid identifiers are unchanged`() {
        "foo".toJavaIdentifier() shouldBe "foo"
        "_foo".toJavaIdentifier() shouldBe "_foo"
        "foo123".toJavaIdentifier() shouldBe "foo123"
    }
}
