package net.xolt.freecam.gradle.dsl

import io.kotest.matchers.shouldBe
import io.mockk.called
import io.mockk.mockk
import io.mockk.verify
import net.xolt.freecam.model.*
import org.gradle.api.Project
import kotlin.test.Test

class ModLibrarySpecTest {

    @Test
    fun `BuildOnlyModLibrarySpec produces expected model`() {
        val project = mockk<Project>()
        val dsl: BuildOnlyLibrarySpec.() -> Unit = {
            group = "g"
            name = "n"
            version = "1"
        }

        val lib = BuildOnlyLibrarySpecImpl(project)
            .apply(dsl)
            .build()

        lib shouldBe BuildOnlyLibrary(
            maven = MavenCoords(group = "g", name = "n", version = "1"),
        )

        verify { project wasNot called }
    }


    @Test
    fun `RuntimeLibrarySpec produces expected model`() {
        val project = mockk<Project>()
        val dsl: RuntimeLibrarySpec.() -> Unit = {
            group = "g"
            name = "n"
            version = "1"
            requires {
                semver = "~1.0"
                maven = "[1,2)"
            }
        }

        val lib = RuntimeLibrarySpecImpl(project)
            .apply(dsl)
            .build()

        lib shouldBe RuntimeLibrary(
            maven = MavenCoords(group = "g", name = "n", version = "1"),
            requires = VersionConstraint(semver = "~1.0", maven = "[1,2)"),
        )

        verify { project wasNot called }
    }

    @Test
    fun `FullLibrarySpec builds expected PublishedLibrary`() {
        val project = mockk<Project>()
        val dsl: FullLibrarySpec.() -> Unit = {
            group = "g"
            name = "n"
            version = "1"
            requires {
                semver = "~1.0"
                maven = "[1,2)"
            }
            publishing {
                type = RelationshipType.REQUIRED
                curseforgeId = "cf"
                modrinthId = "mr"
            }
        }

        val lib = FullLibrarySpecImpl(project)
            .apply(dsl)
            .build()

        lib shouldBe PublishedLibrary(
            maven = MavenCoords(group = "g", name = "n", version = "1"),
            requires = VersionConstraint(semver = "~1.0", maven = "[1,2)"),
            relationship = PublishingRelationship(
                type = RelationshipType.REQUIRED,
                curseforgeId = "cf",
                modrinthId = "mr"
            ),
        )

        verify { project wasNot called }
    }
}