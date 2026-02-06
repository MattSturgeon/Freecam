package net.xolt.freecam.asm

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.xolt.freecam.gradle.ModLibraryDefinition
import net.xolt.freecam.model.ModLibrary
import kotlin.test.Test

private fun def(id: String) = ModLibraryDefinition(
    id = id,
    required = true,
    outType = ModLibrary::class,
    factory = { error("factory must not be invoked during ABI calculation") }
)

class ModLibrariesAbiFingerprintTest {

    data class Fixture(
        val ids: List<String>,
        val abiVersion: Int,
        val expected: String
    )

    private val fixtures = listOf(
        Fixture(listOf("simple"), 1, "3b6bslLby48kdsLjm4ycgA"),
        Fixture(listOf("with space"), 1, "BK8aLzwt3_LqJQri-tK43A"),
        Fixture(listOf("with-dash"), 1, "-jAzCPbQD6bA3Ao3P_4REQ"),
        Fixture(listOf("with.dot"), 1, "WMXKN_ImitUpUkQk4DJ0gw"),
        Fixture(listOf("with_underscore"), 1, "evNTWkAzuYUjdeEJo6xClQ"),
        Fixture(listOf("Ünicode"), 1, "kColPisUMZKctEqtVgfb-g"),
        Fixture(listOf("foo", "bar", "baz"), 1, "7AaLL5a-tNrijRqXI0Y6-A"),
        Fixture(listOf("foo", "bar"), 1, "Z7_kj5zu7QLINUf79Qxs2g"),
        Fixture(listOf("foo", "bar"), 2, "5-hqrF2tKg_s0rOIppwdZQ"),
        Fixture(listOf("foo", "bar"), 11, "FdCae5Y9gCVvLUzawV09sw"),
        Fixture(listOf("foo", "bar"), 99, "2mIOjrtiOAhakBf8G7sG1g"),
        Fixture(listOf("foo", "bar", "baz", "1", "a", "b", "x", "y"), 1, "QK4bTR66W6dZ-X5VdifS7w"),
        Fixture(listOf("a", "b", "c", "d", "e", "f"), 1, "CCBYFs5AP_KH55OxDcMOmw"),
        Fixture(listOf("a1", "2b", "3c", "x4"), 1, "Qq11vI8Okh09Hni-uYIvVw"),
        Fixture(listOf("x", "y", "z"), 1, "gGmASNOoqtEoaNw_6VMYSA"),
        Fixture(listOf("e", "g"), 1, "ig2LtZkRAS4fs0xUrhX2OA"),
    )

    @Test
    fun `fingerprint matches expected fixtures`() = assertSoftly {
        fixtures.forEach { (ids, version, expected) ->
            withClue("ids=$ids abi=$version") {
                val defs = ids.map(::def)
                val fingerprint = ModLibrariesAbiModel(defs, version).fingerprint

                fingerprint shouldBe expected
                fingerprint.length shouldBe 22
            }
        }
    }

    @Test
    fun `fingerprints are insensitive to definition order`() {
        val a = ModLibrariesAbiModel(
            listOf(def("foo"), def("bar"), def("baz")),
            1
        )
        val b = ModLibrariesAbiModel(
            listOf(def("baz"), def("foo"), def("bar")),
            1
        )

        a.fingerprint shouldBe b.fingerprint
    }

    @Test
    fun `fingerprints differ for different library sets`() {
        val base = ModLibrariesAbiModel(
            listOf(def("foo"), def("bar")),
            1
        )
        val extended = ModLibrariesAbiModel(
            listOf(def("foo"), def("bar"), def("baz")),
            1
        )

        base.fingerprint shouldNotBe extended.fingerprint
    }

    @Test
    fun `fingerprints differ for different ABI versions`() {
        val v1 = ModLibrariesAbiModel(
            listOf(def("foo"), def("bar")),
            1
        )
        val v2 = ModLibrariesAbiModel(
            listOf(def("foo"), def("bar")),
            2
        )

        v1.fingerprint shouldNotBe v2.fingerprint
    }
}
