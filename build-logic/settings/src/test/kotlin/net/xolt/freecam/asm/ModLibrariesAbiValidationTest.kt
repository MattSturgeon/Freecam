package net.xolt.freecam.asm

import io.kotest.matchers.string.shouldContain
import net.xolt.freecam.gradle.ModLibraryDefinition
import net.xolt.freecam.model.BuildOnlyLibrary
import net.xolt.freecam.model.ModLibrary
import net.xolt.freecam.model.RuntimeLibrary
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertFailsWith

private fun def(
    id: String,
    outType: KClass<out ModLibrary> = ModLibrary::class,
) = ModLibraryDefinition(
    id = id,
    required = true,
    outType = outType,
    factory = { error("factory must not be invoked during ABI validation") }
)

class ModLibrariesAbiValidationTest {

    @Test
    fun `validate passes for unique library names`() {
        listOf(
            listOf("foo", "bar", "baz"),
            listOf("hello_world", "freecam"),
            listOf("fabric", "forge", "neoForge"),
            listOf("with-dash"),
            listOf("with_underscore"),
            listOf("with.dot"),
            listOf("with space"),
            listOf("Ünicode"),
        ).forEach { ids ->
            val abi = ModLibrariesAbiModel(ids.map(::def))
            abi.validate() // should not throw
        }
    }

    @Test
    fun `validate fails when two library IDs produce same accessor name`() {
        listOf(
            // "foo-bar" and "foo_bar" → getFooBar
            listOf("foo-bar", "foo_bar") to "getFooBar",
            listOf("bar-baz", "bar_baz") to "getBarBaz",
        ).forEach { (ids, expectedAccessor) ->
            val abi = ModLibrariesAbiModel(ids.map(::def))

            val ex = assertFailsWith<IllegalArgumentException> {
                abi.validate()
            }

            ex.message shouldContain expectedAccessor
            ids.forEach { id ->
                ex.message shouldContain id
            }
        }
    }

    @Test
    fun `validate fails when multiple accessor collisions exist`() {
        val ids = listOf(
            "foo-bar", "foo_bar",
            "bar-baz", "bar_baz",
        )

        val abi = ModLibrariesAbiModel(ids.map(::def))

        val ex = assertFailsWith<IllegalArgumentException> {
            abi.validate()
        }

        ex.message shouldContain "getFooBar"
        ex.message shouldContain "getBarBaz"
    }

    @Test
    fun `validate fails for duplicate library IDs`() {
        val abi = ModLibrariesAbiModel(
            listOf(
                def("foo"),
                def("foo"),
            )
        )

        val ex = assertFailsWith<IllegalArgumentException> {
            abi.validate()
        }

        ex.message shouldContain "Multiple library definitions resolve to the same accessor name."
        ex.message shouldContain "getFoo ← 'foo' (ModLibrary), 'foo' (ModLibrary)"
    }

    @Test
    fun `validate fails when same id is defined in multiple scopes`() {
        val abi = ModLibrariesAbiModel(
            listOf(
                def("foo", outType = BuildOnlyLibrary::class),
                def("foo", outType = RuntimeLibrary::class),
            )
        )

        val ex = assertFailsWith<IllegalArgumentException> { abi.validate() }
        ex.message shouldContain "Multiple library definitions resolve to the same accessor name."
        ex.message shouldContain "getFoo ← 'foo' (BuildOnlyLibrary), 'foo' (RuntimeLibrary)"
    }

}
