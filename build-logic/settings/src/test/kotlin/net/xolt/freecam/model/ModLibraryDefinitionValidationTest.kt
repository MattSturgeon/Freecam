package net.xolt.freecam.model

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.xolt.freecam.model.resolution.FieldDefinition
import net.xolt.freecam.model.resolution.FieldDefinitions
import kotlin.test.Test
import kotlin.test.assertFailsWith

private fun zip(vararg pairs: Pair<String, String>): FieldDefinitions =
    def(*(pairs.map { it.first to listOf(it.second) }.toTypedArray()))

private fun def(vararg pairs: Pair<String, List<String>>): Map<String, List<FieldDefinition>> =
    pairs.associateBy(
        keySelector = { it.first },
        valueTransform = { (key, fields) ->
            fields.map { FieldDefinition(path = listOf(key), value = it) }
        }
    )

class ModLibraryDefinitionValidationTest {

    @Test
    fun `resolveFields merges minimal valid library`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v"
        )

        val result = def.resolveFields("foo")

        result shouldBe mapOf(
            "group" to "g",
            "name" to "n",
            "version" to "v"
        )
    }

    @Test
    fun `resolveFields throws for missing required fields`() {
        val def = zip(
            "group" to "g",
            "name" to "n"
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.resolveFields("myLib")
        }

        ex.message shouldContain "Missing required field 'version'"
    }

    @Test
    fun `resolveFields throws for unknown fields`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "unexpected" to "oops"
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.resolveFields("myLib")
        }

        ex.message shouldContain "Unknown field 'unexpected'"
    }

    @Test
    fun `resolveFields throws for relationship_type without ids`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "relationship_type" to "REQUIRED"
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.resolveFields("myLib")
        }

        ex.message shouldContain "curseforge_id or modrinth_id is required"
    }

    @Test
    fun `resolveFields throws for semver_requirement without maven_requirement`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "semver_requirement" to "1.0.0"
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.resolveFields("myLib")
        }

        ex.message shouldContain "maven_requirement is required"
    }

    @Test
    fun `no errors for valid minimal library`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v"
        )

        def.collectMissingKeys() shouldBe emptyList()
        def.collectUnknownKeys() shouldBe emptyList()

        def.resolveFields("foo")
    }

    @Test
    fun `detect missing required fields`() {
        val def = zip(
            "group" to "g",
            "name" to "n"
        )

        val missing = def.collectMissingKeys()
        missing shouldContainExactly listOf(
            "Missing required field 'version'"
        )

        assertFailsWith<IllegalStateException> {
            def.resolveFields("foo")
        }
    }

    @Test
    fun `detect unknown fields`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "foo" to "bar",
            "bar" to "baz"
        )

        val unknown = def.collectUnknownKeys()
        unknown shouldContainExactly listOf(
            "Unknown field 'foo'",
            "Unknown field 'bar'"
        )
    }

    @Test
    fun `relationship_type required when ids defined`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "curseforge_id" to "123"
        )

        val missing = def.collectMissingKeys()
        missing shouldContainExactly listOf(
            "relationship_type is required when curseforge_id or modrinth_id is defined"
        )

        assertFailsWith<IllegalStateException> { def.resolveFields("foo") }
    }

    @Test
    fun `ids required when relationship_type defined`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "relationship_type" to "REQUIRED"
        )

        val missing = def.collectMissingKeys()
        missing shouldContainExactly listOf(
            "curseforge_id or modrinth_id is required when relationship_type is defined"
        )
    }

    @Test
    fun `semver_requirement requires maven_requirement`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "semver_requirement" to "1.0.0"
        )

        val missing = def.collectMissingKeys()
        missing shouldContainExactly listOf(
            "maven_requirement is required when semver_requirement is defined"
        )
    }

    @Test
    fun `maven_requirement requires semver_requirement`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "version" to "v",
            "maven_requirement" to "g:n:v"
        )

        val missing = def.collectMissingKeys()
        missing shouldContainExactly listOf(
            "semver_requirement is required when maven_requirement is defined"
        )
    }

    @Test
    fun `multiple errors collected`() {
        val def = zip(
            "group" to "g",
            "name" to "n",
            "semver_requirement" to "1.0.0",
            "foo" to "bar"
        )

        val errors = def.collectMissingKeys() + def.collectUnknownKeys()
        errors shouldContainExactly listOf(
            "Missing required field 'version'",
            "maven_requirement is required when semver_requirement is defined",
            "Unknown field 'foo'"
        )
    }

    @Test
    fun `duplicate identical values are merged`() {
        val def = def(
            "group" to listOf("g", "g"),
            "name" to listOf("n"),
            "version" to listOf("v", "v", "v")
        )

        val result = def.resolveFields("foo")

        result shouldBe mapOf(
            "group" to "g",
            "name" to "n",
            "version" to "v"
        )
    }

    @Test
    fun `conflicting duplicate values throw`() {
        val def = def(
            "group" to listOf("g1", "g2"),
            "name" to listOf("n"),
            "version" to listOf("v")
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.resolveFields("foo")
        }

        ex.message shouldContain "Conflicting values for field 'group'"
        ex.message shouldContain "group: g1"
        ex.message shouldContain "group: g2"
    }

    @Test
    fun `conflicts and schema errors are both reported`() {
        val def = def(
            "group" to listOf("g1", "g2"),
            "name" to listOf("n"),
            "foo" to listOf("bar")
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.resolveFields("foo")
        }

        ex.message shouldContain "Conflicting values for field 'group'"
        ex.message shouldContain "group: g1"
        ex.message shouldContain "group: g2"
        ex.message shouldContain "Missing required field 'version'"
        ex.message shouldContain "Unknown field 'foo'"
    }

}