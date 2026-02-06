package net.xolt.freecam.asm

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ToPascalCaseTest {

    @Test
    fun `simple identifiers are capitalised`() {
        "foo".toPascalCase() shouldBe "Foo"
        "barBaz".toPascalCase() shouldBe "BarBaz"
    }

    @Test
    fun `underscores define word boundaries`() {
        "foo_bar".toPascalCase() shouldBe "FooBar"
        "foo__bar".toPascalCase() shouldBe "FooBar"
        "_foo_bar_".toPascalCase() shouldBe "FooBar"
    }

    @Test
    fun `hyphens define word boundaries`() {
        "foo-bar".toPascalCase() shouldBe "FooBar"
        "foo--bar".toPascalCase() shouldBe "FooBar"
    }

    @Test
    fun `whitespace defines word boundaries`() {
        "foo bar".toPascalCase() shouldBe "FooBar"
        "foo   bar".toPascalCase() shouldBe "FooBar"
        "  foo bar  ".toPascalCase() shouldBe "FooBar"
    }

    @Test
    fun `non identifier punctuation is preserved`() {
        "foo.bar".toPascalCase() shouldBe "Foo.bar"
        "foo@bar#baz".toPascalCase() shouldBe "Foo@bar#baz"
    }

    @Test
    fun `digits are preserved within words`() {
        "fabric_api_v2".toPascalCase() shouldBe "FabricApiV2"
        "1foo_2bar".toPascalCase() shouldBe "1foo2bar"
    }

    @Test
    fun `unicode and punctuation handled correctly`() {
        "weird.input!".toPascalCase() shouldBe "Weird.input!"
        "Ünicode".toPascalCase() shouldBe "Ünicode"
    }

    @Test
    fun `mixed separators are handled`() {
        "foo_bar-baz".toPascalCase() shouldBe "FooBarBaz"
    }

    @Test
    fun `mixed whitespace and separators are handled`() {
        "foo-_ bar".toPascalCase() shouldBe "FooBar"
        "foo - bar_baz".toPascalCase() shouldBe "FooBarBaz"
    }

    @Test
    fun `PascalCase is idempotent`() {
        "FabricApi".toPascalCase() shouldBe "FabricApi"
    }

    @Test
    fun `empty or invalid input collapses safely`() {
        "".toPascalCase() shouldBe ""
        "___".toPascalCase() shouldBe ""
        "---".toPascalCase() shouldBe ""
    }
}