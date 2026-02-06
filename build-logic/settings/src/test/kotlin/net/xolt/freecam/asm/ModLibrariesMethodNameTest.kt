package net.xolt.freecam.asm

import io.kotest.matchers.shouldBe
import net.xolt.freecam.gradle.ModLibraryDefinition
import net.xolt.freecam.model.ModLibrary
import kotlin.test.Test

private fun def(id: String) = ModLibraryDefinition(
    id = id,
    required = true,
    outType = ModLibrary::class,
    factory = { error("factory must not be invoked during ASM tests") }
)

class ModLibrariesMethodNameTest {

    @Test
    fun `library definitions are converted to sorted accessor method names`() {
        val defs = listOf(
            def("foo-bar"),
            def("baz_qux"),
            def("x y z"),
        )

        val abi = ModLibrariesAbiModel(
            libraries = defs,
            abiVersion = 1,
        )

        val names = abi.accessors.keys

        names shouldBe listOf(
            "getBazQux",
            "getFooBar",
            "getXYZ",
        )
    }

    @Test
    fun `accessor name sanitization removes illegal identifier characters`() {
        val defs = listOf(
            def("hello.world!"),
            def("123abc"),
        )

        val abi = ModLibrariesAbiModel(defs, abiVersion = 1)

        abi.accessors.keys shouldBe listOf(
            "get123abc",
            "getHello_world_",
        )
    }
}
