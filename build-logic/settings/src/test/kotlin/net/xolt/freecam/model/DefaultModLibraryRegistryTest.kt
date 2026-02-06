package net.xolt.freecam.model

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.called
import io.mockk.mockk
import io.mockk.verify
import net.xolt.freecam.gradle.ModLibraryDefinition
import net.xolt.freecam.gradle.dsl.BuildOnlyLibrarySpecImpl
import org.gradle.api.Project
import kotlin.test.Test

class DefaultModLibraryRegistryTest {

    private fun publishedFooFactory(): (Project) -> PublishedLibrary = {
        PublishedLibrary(
            maven = MavenCoords(
                group = "g",
                name = "n",
                version = "1"
            ),
            requires = VersionConstraint(
                semver = "~1.0",
                maven = "[1,2)"
            ),
            relationship = PublishingRelationship(
                type = RelationshipType.REQUIRED,
                curseforgeId = "cf",
                modrinthId = "mr"
            )
        )
    }

    private val fooDef = ModLibraryDefinition(
        id = "foo",
        required = true,
        outType = PublishedLibrary::class,
        factory = publishedFooFactory()
    )

    @Test
    fun `registry provides expected PublishedLibrary`() {
        val project = mockk<Project>()

        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(fooDef).associateBy { it.id },
        )

        registry["foo"] shouldBe PublishedLibrary(
            maven = MavenCoords(
                group = "g",
                name = "n",
                version = "1"
            ),
            requires = VersionConstraint(
                semver = "~1.0",
                maven = "[1,2)"
            ),
            relationship = PublishingRelationship(
                type = RelationshipType.REQUIRED,
                curseforgeId = "cf",
                modrinthId = "mr"
            )
        )

        verify { project wasNot called }
    }

    @Test
    fun `get returns cached instance on subsequent calls`() {
        val project = mockk<Project>()
        var buildCount = 0

        val def = fooDef.copy(
            factory = {
                buildCount++
                publishedFooFactory().invoke(it)
            }
        )

        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(def).associateBy { it.id },
        )

        val first = registry["foo"]
        repeat(128) {
            registry["foo"] shouldBeSameInstanceAs first
        }

        buildCount shouldBe 1
        verify { project wasNot called }
    }

    @Test
    fun `registry throws for unknown library id`() {
        val project = mockk<Project>()
        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(fooDef).associateBy { it.id },
        )

        val ex = shouldThrowExactly<IllegalArgumentException> {
            registry["unknown-id"]
        }

        ex.message shouldBe "No library registered with ID 'unknown-id'"
        verify { project wasNot called }
    }

    @Test
    fun `required library throws when factory returns null`() {
        val project = mockk<Project>()

        val def = fooDef.copy(factory = { null })

        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(def).associateBy { it.id },
        )

        shouldThrowExactly<NullPointerException> {
            registry["foo"]
        }
    }

    @Test
    fun `optional library returns null when unavailable`() {
        val project = mockk<Project>()

        val def = fooDef.copy(
            required = false,
            factory = { null }
        )

        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(def).associateBy { it.id },
        )

        registry["foo"] shouldBe null
    }

    @Test
    fun `registry does not hide type mismatch`() {
        val project = mockk<Project>()

        @Suppress("UNCHECKED_CAST")
        val def = (fooDef as ModLibraryDefinition<ModLibrary>).copy(
            factory = {
                BuildOnlyLibrary(
                    maven = MavenCoords(
                        group = "g",
                        name = "n",
                        version = "1"
                    ),
                ) as ModLibrary
            },
        )

        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(def).associateBy { it.id },
        )

        shouldThrowExactly<ClassCastException> {
            registry["foo"]
        }
    }

    @Test
    fun `required library throws when spec unavailable due to defaultAvailability`() {
        val project = mockk<Project>()

        val def = ModLibraryDefinition(
            id = "foo",
            required = true,
            outType = BuildOnlyLibrary::class,
            factory = { BuildOnlyLibrarySpecImpl(project).build() }
        )

        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(def).associateBy { it.id }
        )

        shouldThrowExactly<NullPointerException> {
            registry["foo"]
        }
    }

    @Test
    fun `optional library returns null when unavailable due to defaultAvailability`() {
        val project = mockk<Project>()

        val def = ModLibraryDefinition(
            id = "foo",
            required = false,
            outType = BuildOnlyLibrary::class,
            factory = { BuildOnlyLibrarySpecImpl(project).build() }
        )

        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(def).associateBy { it.id }
        )

        registry["foo"] shouldBe null
    }

    @Test
    fun `optional library returns null when availableWhen disables it`() {
        val project = mockk<Project>()

        val def = ModLibraryDefinition(
            id = "foo",
            required = false,
            outType = BuildOnlyLibrary::class,
            factory = {
                BuildOnlyLibrarySpecImpl(project).apply {
                    group = "g"
                    name = "n"
                    version = "1"
                    availableWhen { false }
                }.build()
            }
        )

        val registry = DefaultModLibraryRegistry(
            project = project,
            definitions = listOf(def).associateBy { it.id }
        )

        registry["foo"] shouldBe null
    }
}
