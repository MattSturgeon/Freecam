package net.xolt.freecam.gradle.dsl

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import net.xolt.freecam.model.BuildOnlyLibrary
import net.xolt.freecam.model.MavenCoords
import org.gradle.api.Project
import kotlin.test.Test

class ModLibraryAvailabilityTest {

    @Test
    fun `BuildOnlyLibrarySpec unavailable when maven incomplete`() {
        val project = mockk<Project>()
        val spec = BuildOnlyLibrarySpecImpl(project).apply {
            group = "g"
            name = "n"
            // version missing
        }
        spec.build() shouldBe null
    }

    @Test
    fun `RuntimeLibrarySpec unavailable when requires incomplete`() {
        val project = mockk<Project>()
        val spec = RuntimeLibrarySpecImpl(project).apply {
            group = "g"
            name = "n"
            version = "1"
            requires {
                semver = "~1.0"
                // maven missing
            }
        }
        spec.build() shouldBe null
    }

    @Test
    fun `FullLibrarySpec unavailable when publishing incomplete`() {
        val project = mockk<Project>()
        val spec = FullLibrarySpecImpl(project).apply {
            group = "g"
            name = "n"
            version = "1"
            requires {
                semver = "~1.0"
                maven = "[1,2)"
            }
            publishing {
                type = null
                curseforgeId = "cf"
                modrinthId = "mr"
            }
        }
        spec.build() shouldBe null
    }

    @Test
    fun `availableWhen false disables library`() {
        val project = mockk<Project>()
        val spec = BuildOnlyLibrarySpecImpl(project).apply {
            group = "g"
            name = "n"
            version = "1"
            availableWhen { false }
        }
        spec.build() shouldBe null
    }

    @Test
    fun `availability predicates are ANDed`() {
        val project = mockk<Project>()
        val spec = BuildOnlyLibrarySpecImpl(project).apply {
            group = "g"
            name = "n"
            version = "1"
            availableWhen { true }
            availableWhen { false }
            // true AND false = false
        }
        spec.build() shouldBe null
    }

    @Test
    fun `predicate sees final DSL state`() {
        val project = mockk<Project>()
        var capture: String? = "g0"
        val spec = BuildOnlyLibrarySpecImpl(project)
            .apply {
                group = "g1"
                name = "n"
                version = "1"
                availableWhen {
                    capture = group
                    true
                }
            }
            .apply {
                group = "g2"
            }
        val lib = spec.build()
        lib shouldBe BuildOnlyLibrary(
            maven = MavenCoords(group = "g2", name = "n", version = "1"),
        )
        capture shouldBe "g2"
    }
}
