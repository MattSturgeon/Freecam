package net.xolt.freecam.model

import dev.eav.tomlkt.Toml
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.Test

private val toml = Toml {
    ignoreUnknownKeys = false
}

class ModLibraryDefinitionTest {

    @Test
    fun `empty table produces empty overrides and freeform`() {
        val input = ""
        val def = toml.decodeFromString<ModLibraryDefinition>(input)
        def.mcOverrides shouldBe emptyMap()
        def.loaderOverrides shouldBe emptyMap()
        def.freeform shouldBe emptyMap()
    }

    @Test
    fun `freeform-only table deserializes`() {
        val input = """
            foo = "bar"
            baz = "qux"
        """.trimIndent()

        val def = toml.decodeFromString<ModLibraryDefinition>(input)

        def.mcOverrides shouldBe emptyMap()
        def.loaderOverrides shouldBe emptyMap()
        def.freeform shouldBe mapOf(
            "foo" to "bar",
            "baz" to "qux"
        )
    }

    @Test
    fun `mc overrides and freeform coexist`() {
        val input = """
        mc.foo.bar = "baz"
        custom = "value"
    """.trimIndent()

        val def = toml.decodeFromString<ModLibraryDefinition>(input)

        def.mcOverrides.keys shouldBe setOf("foo")
        def.freeform shouldBe mapOf("custom" to "value")
    }

    @Test
    fun `mc and loader overrides with freeform`() {
        val input = """
        mc.a.b = "1"
        loader.x.y = "2"
        note = "hello"
    """.trimIndent()

        val def = toml.decodeFromString<ModLibraryDefinition>(input)

        def.mcOverrides.containsKey("a") shouldBe true
        def.loaderOverrides.containsKey("x") shouldBe true
        def.freeform shouldBe mapOf("note" to "hello")
    }

    @Test
    fun `nested overrides deserialize recursively`() {
        val input = """
        mc.foo.loader.bar.leaf = "value"
    """.trimIndent()

        val def = toml.decodeFromString<ModLibraryDefinition>(input)

        val outer = def.mcOverrides["foo"]!!
        val inner = outer.loaderOverrides["bar"]!!

        inner.freeform shouldBe mapOf("leaf" to "value")
    }

    @Test
    fun `round-trip preserves semantic structure`() {
        val input = """
        mc.a.b = "1"
        loader.x.y = "2"
        note = "hello"
    """.trimIndent()

        val decoded = toml.decodeFromString<ModLibraryDefinition>(input)
        val encoded = toml.encodeToString<ModLibraryDefinition>(decoded)
        val decodedAgain = toml.decodeFromString<ModLibraryDefinition>(encoded)

        decodedAgain shouldBe decoded
    }

    @Test
    fun `non-string freeform fails`() {
        val input = """
        bad = { x = 1 }
    """.trimIndent()

        kotlin.test.assertFails {
            toml.decodeFromString<ModLibraryDefinition>(input)
        }
    }
}
