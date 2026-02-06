package net.xolt.freecam.gradle.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import net.xolt.freecam.gradle.ModLibrariesSettingsExtension
import org.gradle.api.Project
import kotlin.test.Test

class ModLibrariesSettingsExtensionDslTest {

    private class TestExtension : ModLibrariesSettingsExtension()

    @Test
    fun `buildOnly registers definition correctly`() {
        val ext = TestExtension()

        ext.buildOnly("foo") {
            // some dummy DSL mutation
            println("applied")
        }

        val defs = ext.validatedLibrarySpecs()

        defs.size shouldBe 1
        defs.map { it.id } shouldBe listOf("foo")
    }

    @Test
    fun `runtime registers definition correctly`() {
        val ext = TestExtension()

        ext.runtime("bar") {}
        val defs = ext.validatedLibrarySpecs()

        defs.size shouldBe 1
        defs.map { it.id } shouldBe listOf("bar")
    }

    @Test
    fun `published registers definition correctly`() {
        val ext = TestExtension()

        ext.published("baz") {}
        val defs = ext.validatedLibrarySpecs()

        defs.size shouldBe 1
        defs.map { it.id } shouldBe listOf("baz")
    }

    @Test
    fun `conflicting required flags throw`() {
        val ext = TestExtension()

        ext.buildOnly("conflict", required = true) {}
        ext.buildOnly("conflict", required = false) {}

        val ex = shouldThrow<IllegalArgumentException> {
            ext.validatedLibrarySpecs()
        }

        ex.message shouldBe "Conflicting `required` definitions for 'conflict'"
    }

    @Test
    fun `DSL block is applied lazily`() {
        val project = mockk<Project>()

        val ext = TestExtension()
        var applied = false

        ext.buildOnly("lazy") {
            applied = true
            group = "g"
            name = "lazy"
            version = "1"
        }
        applied shouldBe false

        val spec = ext.validatedLibrarySpecs().single()
        applied shouldBe false

        // Evaluate
        spec.factory(project)
        applied shouldBe true
    }
}
