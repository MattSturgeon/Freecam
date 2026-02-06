package net.xolt.freecam.gradle

import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.xolt.freecam.model.*
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.inputStream
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ModLibrariesSettingsPluginTest {

    private lateinit var testDir: Path

    @BeforeTest
    fun setup() {
        testDir = createTempDirectory(javaClass.simpleName)
    }

    @AfterTest
    fun teardown() {
        testDir.toFile().deleteRecursively()
    }

    @Test
    @OptIn(ExperimentalSerializationApi::class)
    fun `generated accessor writes expected output as JSON`() {
        // settings.gradle.kts: apply plugin & define libraries
        testDir.resolve("settings.gradle.kts").writeText(
            """
            import net.xolt.freecam.model.RelationshipType

            plugins {
                id("freecam.modlibraries")
            }

            modLibraries {
                buildOnly("foo") {
                    group = "g"
                    name = "foo"
                    version = "v1"
                }
                runtime("bar") {
                    group = "g"
                    name = "bar"
                    version = "v2"
                    requires {
                        semver = "~0.9"
                        maven = "[0.9,)"
                    }
                }
                published("baz") {
                    group = "g"
                    name = "baz"
                    version = "v3"
                    requires {
                        semver = "~0.9"
                        maven = "[0.9,)"
                    }
                    publishing {
                        type = RelationshipType.REQUIRED
                        curseforgeId = "1234"
                        modrinthId = "abcd"
                    }
                }
            }
            """.trimIndent()
        )

        // build.gradle.kts: task writes accessor output to a file
        testDir.resolve("build.gradle.kts").writeText(
            """
            import kotlinx.serialization.json.Json

            tasks.register("checkAccessor") {
                val file = projectDir.resolve("libraries.json")
                doLast {
                    val json = Json.encodeToString(mapOf(
                        "foo" to modLibraries.foo,
                        "bar" to modLibraries.bar,
                        "baz" to modLibraries.baz,
                    ))
                    file.parentFile.mkdirs()
                    file.writeText(json)
                }
            }
            """.trimIndent()
        )

        // Run Gradle
        val result = GradleRunner.create()
            .withProjectDir(testDir.toFile())
            .withPluginClasspath()
            .withArguments("checkAccessor")
            .build()

        val outcome = result.task(":checkAccessor")?.outcome.toString()

        outcome shouldBe "SUCCESS"

        val libraries = testDir.resolve("libraries.json").inputStream().let {
            Json.decodeFromStream<Map<String, ModLibrary>>(it)
        }

        libraries shouldBe mapOf(
            "foo" to BuildOnlyLibrary(
                maven = MavenCoords(group = "g", name = "foo", version = "v1"),
            ),
            "bar" to RuntimeLibrary(
                maven = MavenCoords(group = "g", name = "bar", version = "v2"),
                requires = VersionConstraint(semver = "~0.9", maven = "[0.9,)"),
            ),
            "baz" to PublishedLibrary(
                maven = MavenCoords(group = "g", name = "baz", version = "v3"),
                requires = VersionConstraint(semver = "~0.9", maven = "[0.9,)"),
                relationship = PublishingRelationship(
                    type = RelationshipType.REQUIRED,
                    curseforgeId = "1234",
                    modrinthId = "abcd",
                ),
            ),
        )
    }
}
