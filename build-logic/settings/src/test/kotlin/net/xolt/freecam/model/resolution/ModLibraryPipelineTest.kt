package net.xolt.freecam.model.resolution

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.serialization.SerializationException
import net.xolt.freecam.model.ModLibrariesFile
import net.xolt.freecam.model.ModLibrary
import net.xolt.freecam.model.ModLibraryDefinition
import net.xolt.freecam.model.ModRelationship
import net.xolt.freecam.model.resolveFields
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ModLibraryPipelineTest {

    @Test
    fun `buildModLibrary produces ModLibrary with correct merged fields`() {
        val def = ModLibraryDefinition(
            freeform = mapOf(
                "group" to "com.example",
                "name" to "lib",
                "version" to "1.0.0",
                "curseforge_id" to "123",
                "relationship_type" to "required"
            )
        )

        val lib = def.buildModLibrary("lib-id", "fabric", "1.20")

        lib.id shouldBe "lib-id"
        lib.group shouldBe "com.example"
        lib.name shouldBe "lib"
        lib.version shouldBe "1.0.0"
        lib.relationship?.type shouldBe ModRelationship.Type.REQUIRED
        lib.relationship?.curseforgeId shouldBe "123"
        lib.relationship?.modrinthId shouldBe null
        lib.requirement shouldBe null
    }

    @Test
    fun `buildModLibrary includes optional requirement when semver and maven specified`() {
        val def = ModLibraryDefinition(
            freeform = mapOf(
                "group" to "g",
                "name" to "n",
                "version" to "v",
                "semver_requirement" to "1.0.0",
                "maven_requirement" to "g:n:v"
            )
        )

        val lib = def.buildModLibrary("id", "fabric", "1.20")
        lib.requirement?.semver shouldBe "1.0.0"
        lib.requirement?.maven shouldBe "g:n:v"
    }

    @Test
    fun `loader-only override applies`() {
        val def = ModLibraryDefinition(
            freeform = mapOf(
                "group" to "g",
                "name" to "n",
                "version" to "v",
            ),
            loaderOverrides = mapOf(
                "fabric" to ModLibraryDefinition(
                    freeform = mapOf("version" to "v2")
                )
            )
        )

        val result = def.buildModLibrary("lib", loader = "fabric", mcVersion = "1.20")

        result.version shouldBe "v2"
    }

    @Test
    fun `mc-only override applies`() {
        val def = ModLibraryDefinition(
            freeform = mapOf(
                "group" to "g",
                "name" to "n",
                "version" to "v",
            ),
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = mapOf("version" to "v120")
                )
            )
        )

        val result = def.buildModLibrary("lib", loader = "fabric", mcVersion = "1.20")

        result.version shouldBe "v120"
    }

    @Test
    fun `empty override layer is transparent`() {
        val def = ModLibraryDefinition(
            freeform = mapOf(
                "group" to "g",
                "name" to "n",
                "version" to "v",
            ),
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = emptyMap()
                )
            )
        )

        val result = def.buildModLibrary("lib", loader = "fabric", mcVersion = "1.20")

        result.version shouldBe "v"
    }

    @Test
    fun `conflicting highest-priority fields throws when building ModLibrary`() {
        val def = ModLibraryDefinition(
            freeform = mapOf("group" to "g1"),
            loaderOverrides = mapOf("fabric" to ModLibraryDefinition(freeform = mapOf("group" to "g2"))),
            mcOverrides = mapOf("1.20" to ModLibraryDefinition(freeform = mapOf("group" to "g3")))
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.buildModLibrary("lib-id", "fabric", "1.20")
        }

        ex.message shouldContain "Conflicting values for field 'group'"
        ex.message shouldNotContain "group: g1"
        ex.message shouldContain "  - loader.fabric.group: g2"
        ex.message shouldContain "  - mc.\"1.20\".group: g3"
    }

    @Test
    fun `depth-first override selection respects both loader and mcVersion`() {
        val root = ModLibraryDefinition(
            freeform = mapOf("f" to "root", "x" to "y"),
            mcOverrides = mapOf("1.20" to ModLibraryDefinition(freeform = mapOf("f" to "mc"))),
            loaderOverrides = mapOf("fabric" to ModLibraryDefinition(freeform = mapOf("f" to "loader"))),
        )

        val fields = root.selectHighestPriorityFields("fabric", "1.20")

        // Depth 1 (highest priority) fields should be present
        // Depth 0 should be missing; shadowed by depth 1
        fields["f"]?.map(FieldDefinition::value) shouldBe listOf("mc", "loader")

        // Depth 0 should be present if not shadowed
        fields["x"]?.map(FieldDefinition::value) shouldBe listOf("y")
    }

    @Test
    fun `buildModLibrary produces expected ModLibrary instances`() {
        // A mini catalog with two libraries, "foo" and "bar"
        val miniFile = ModLibrariesFile(
            libraries = mapOf(
                "foo" to ModLibraryDefinition(
                    freeform = mapOf(
                        "group" to "shadowed by fabric loader override",
                        "name" to "foo",
                        "version" to "shadowed by 1.20 mc override"
                    ),
                    mcOverrides = mapOf(
                        "1.20" to ModLibraryDefinition(
                            freeform = mapOf(
                                "version" to "1.1"
                            )
                        )
                    ),
                    loaderOverrides = mapOf(
                        "fabric" to ModLibraryDefinition(
                            freeform = mapOf(
                                "group" to "g1f"
                            )
                        )
                    )
                ),
                "bar" to ModLibraryDefinition(
                    freeform = mapOf(
                        "group" to "g2",
                        "name" to "bar",
                        "version" to "2.0"
                    )
                )
            )
        )

        // Manually resolve each library
        // Avoid using ModLibrariesFile.buildCatalog() because it asserts the _actual_ ModLibraries catalog fields are defined
        val resolved = miniFile.libraries.mapValues { (id, def) ->
            def.buildModLibrary(id, loader = "fabric", mcVersion = "1.20")
        }

        // Assertions: field merging / override behavior
        resolved["foo"] shouldBe ModLibrary(
            id = "foo",
            group = "g1f",   // loader override shadows root
            name = "foo",    // root value
            version = "1.1", // MC override applied
        )

        resolved["bar"] shouldBe ModLibrary(
            id = "bar",
            group = "g2",   // loader override shadows root
            name = "bar",    // root value
            version = "2.0", // MC override applied
        )
    }

    @Test
    fun `buildModLibrary respects permutation selection and priority shadowing`() {
        val miniFile = ModLibrariesFile(
            libraries = mapOf(
                "lib1" to ModLibraryDefinition(
                    freeform = mapOf("group" to "g", "name" to "lib1", "version" to "1.0"),
                    mcOverrides = mapOf(
                        "1.20" to ModLibraryDefinition(freeform = mapOf("version" to "1.1")),
                        "1.19" to ModLibraryDefinition(freeform = mapOf("version" to "0.9"))
                    ),
                    loaderOverrides = mapOf(
                        "fabric" to ModLibraryDefinition(freeform = mapOf("group" to "gf")),
                        "forge" to ModLibraryDefinition(freeform = mapOf("group" to "gf2"))
                    )
                ),
                "lib2" to ModLibraryDefinition(
                    freeform = mapOf("group" to "g2", "name" to "lib2", "version" to "2.0")
                )
            )
        )

        val resolved = miniFile.libraries.mapValues { (id, def) ->
            def.buildModLibrary(id, loader = "fabric", mcVersion = "1.20")
        }

        val lib1 = resolved["lib1"]!!
        // Only selected permutation values appear
        lib1.group shouldBe "gf"          // loader override selected
        lib1.version shouldBe "1.1"       // mc override selected
        lib1.name shouldBe "lib1"         // root value

        val lib2 = resolved["lib2"]!!
        lib2.group shouldBe "g2"
        lib2.version shouldBe "2.0"
    }

    @Test
    fun `buildModLibrary throws for conflicting definitions`() {
        val def = ModLibraryDefinition(
            freeform = mapOf(
                "group" to "g1",
                "name" to "foo",
                "version" to "1.0",
            ),
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = mapOf("group" to "g2") // conflict with loader override
                )
            ),
            loaderOverrides = mapOf(
                "fabric" to ModLibraryDefinition(
                    freeform = mapOf("group" to "g3") // conflict with mc version override
                )
            ),
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.buildModLibrary("foo", loader = "fabric", mcVersion = "1.20")
        }

        ex.message shouldContain "Conflicting values for field 'group'"
        ex.message shouldNotContain "  - group: g1"
        ex.message shouldContain "  - mc.\"1.20\".group: g2"
        ex.message shouldContain "  - loader.fabric.group: g3"
    }

    @Test
    fun `buildModLibrary throws for missing required fields`() {
        val def = ModLibraryDefinition(
            freeform = mapOf("group" to "g")
        )

        val ex = assertFailsWith<IllegalStateException> {
            def.buildModLibrary("foo", loader = "fabric", mcVersion = "1.20")
        }

        ex.message shouldContain "Missing required field 'name'"
        ex.message shouldContain "Missing required field 'version'"
    }

    @Test
    fun `invalid relationship_type reports error`() {
        val def = ModLibraryDefinition(
            freeform = mapOf(
                "group" to "g",
                "name" to "n",
                "version" to "v",
                "relationship_type" to "not-a-real-type",
                "modrinth_id" to "abc"
            )
        )

        val ex = assertFailsWith<SerializationException> {
            def.buildModLibrary("lib", "fabric", "1.20")
        }

        val className = ModRelationship.Type::class.qualifiedName
        ex.message shouldBe "$className does not contain element with name 'not-a-real-type' at field 'relationship_type'"
    }

    @Test
    fun `higher priority override does not resurrect lower priority fields`() {
        val def = ModLibraryDefinition(
            freeform = mapOf(
                "group" to "g",
                "name" to "low",
                "version" to "v1",
            ),
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = mapOf(
                        "name" to "mid",
                        "version" to "v2",
                    ),
                    loaderOverrides = mapOf(
                        "fabric" to ModLibraryDefinition(
                            freeform = mapOf(
                                // note: no "name" here
                                "version" to "v3",
                            )
                        )
                    )
                )
            )
        )

        val merged = def.selectHighestPriorityFields(loader = "fabric", mcVersion = "1.20")
        val resolved = merged.resolveFields("lib")

        resolved shouldBe mapOf(
            "group" to "g",
            "name" to "mid",
            "version" to "v3",
        )
    }
}
