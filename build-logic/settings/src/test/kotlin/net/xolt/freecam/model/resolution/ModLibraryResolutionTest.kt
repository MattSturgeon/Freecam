package net.xolt.freecam.model.resolution

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import net.xolt.freecam.model.ModLibraryDefinition
import kotlin.test.Test

class ModLibraryResolutionTest {

    @Test
    fun `selectHighestPriorityFields zips definitions by depth`() {
        val root = ModLibraryDefinition(
            freeform = mapOf("group" to "g"),
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = mapOf("group" to "g")
                )
            ),
            loaderOverrides = mapOf(
                "fabric" to ModLibraryDefinition(
                    freeform = mapOf("group" to "g")
                )
            )
        )

        val zipped = root.selectHighestPriorityFields("fabric", "1.20")

        // The deepest layer that defines `group` has `group=g` twice
        zipped["group"]?.map(FieldDefinition::value) shouldBe listOf("g", "g")
    }

    @Test
    fun `selectHighestPriorityFields resolves conflicts by priority`() {
        val root = ModLibraryDefinition(
            freeform = mapOf(
                "field" to "root",
                "other" to "abc", // Shadowed by mc."1.20".other
            ),
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = mapOf(
                        "field" to "mc1", // Conflicts with loader.fabric.field
                        "other" to "xyz", // Overrides root other=abc
                    )
                )
            ),
            loaderOverrides = mapOf(
                "fabric" to ModLibraryDefinition(
                    freeform = mapOf(
                        "field" to "loader1", // Conflicts with mc."1.20".field
                    ),
                    mcOverrides = mapOf(
                        "1.20" to ModLibraryDefinition(
                            freeform = mapOf(
                                "field" to "loader1_mc2", // Resolves conflict with highly-specific override
                            )
                        )
                    ),
                )
            )
        )

        val fields = root.selectHighestPriorityFields(loader = "fabric", mcVersion = "1.20")

        fields shouldBe mapOf(
            // high prio overrides medium prio conflict
            "field" to listOf(FieldDefinition(
                path = listOf("loader", "fabric", "mc", "1.20", "field"),
                value = "loader1_mc2",
            )),
            // medium prio overrides root
            "other" to listOf(FieldDefinition(
                path = listOf("mc", "1.20", "other"),
                value = "xyz",
            )),
        )
    }

    @Test
    fun `selectHighestPriorityFields merges same-priority definitions`() {
        val root = ModLibraryDefinition(
            freeform = mapOf("group" to "shadowed"),
            loaderOverrides = mapOf("fabric" to ModLibraryDefinition(freeform = mapOf("group" to "g"))),
            mcOverrides = mapOf("1.20" to ModLibraryDefinition(freeform = mapOf("group" to "g"))),
        )

        val zipped = root.selectHighestPriorityFields("fabric", "1.20")
        zipped["group"]?.map(FieldDefinition::value) shouldBe listOf("g", "g") // duplicate but identical
    }

    @Test
    fun `selectDefinitionsRecursive emits definitions in depth-first order`() {
        val root = ModLibraryDefinition(
            freeform = mapOf("a" to "root"),
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = mapOf("a" to "mc")
                )
            ),
            loaderOverrides = mapOf(
                "fabric" to ModLibraryDefinition(
                    freeform = mapOf("a" to "loader")
                )
            )
        )

        val out = root.selectDefinitionsRecursive("fabric", "1.20")
        val depthToField = out.map { it.path.size to it.definition.freeform["a"] }

        depthToField shouldContainExactlyInAnyOrder listOf(
            0 to "root",
            2 to "mc",
            2 to "loader",
        )
    }

    @Test
    fun `selectDefinitionsRecursive ignores unrelated overrides`() {
        val root = ModLibraryDefinition(
            freeform = mapOf("f" to "root"),
            mcOverrides = mapOf(
                "1.19" to ModLibraryDefinition(freeform = mapOf("f" to "mc19")),
                "1.20" to ModLibraryDefinition(freeform = mapOf("f" to "mc20"))
            ),
            loaderOverrides = mapOf(
                "forge" to ModLibraryDefinition(freeform = mapOf("f" to "forge")),
                "fabric" to ModLibraryDefinition(freeform = mapOf("f" to "fabric"))
            )
        )

        val out = root.selectDefinitionsRecursive(loader = "fabric", mcVersion = "1.20")
        val depthToField = out.map { it.path.size to it.definition.freeform["f"] }

        // Only the root, 1.20 MC override, and fabric loader override are selected
        depthToField shouldContainExactlyInAnyOrder listOf(
            0 to "root",
            2 to "mc20",
            2 to "fabric",
        )
    }

    @Test
    fun `selectDefinitionsRecursive preserves root and child overrides`() {
        val root = ModLibraryDefinition(
            freeform = mapOf("field" to "root"),
            mcOverrides = mapOf("1.20" to ModLibraryDefinition(freeform = mapOf("field" to "mc"))),
            loaderOverrides = mapOf("fabric" to ModLibraryDefinition(freeform = mapOf("field" to "loader")))
        )

        val nodes = root.selectDefinitionsRecursive("fabric", "1.20")
        nodes.map { it.path.size to it.definition.freeform["field"] } shouldBe listOf(
            0 to "root",
            2 to "mc",
            2 to "loader",
        )
    }

    @Test
    fun `paths reflect nested mc then loader overrides`() {
        val def = ModLibraryDefinition(
            freeform = mapOf("a" to "root"),
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = mapOf("a" to "mc"),
                    loaderOverrides = mapOf(
                        "fabric" to ModLibraryDefinition(
                            freeform = mapOf("a" to "mc_loader")
                        )
                    )
                )
            )
        )

        val fields = def.selectHighestPriorityFields("fabric", "1.20")

        fields["a"] shouldBe listOf(
            FieldDefinition(
                path = listOf("mc", "1.20", "loader", "fabric", "a"),
                value = "mc_loader"
            )
        )
    }

    @Test
    fun `identical values at same priority retain distinct paths`() {
        val def = ModLibraryDefinition(
            mcOverrides = mapOf(
                "1.20" to ModLibraryDefinition(
                    freeform = mapOf("group" to "g"),
                ),
            ),
            loaderOverrides = mapOf(
                "fabric" to ModLibraryDefinition(freeform = mapOf("group" to "g"))
            )
        )

        val fields = def.selectHighestPriorityFields("fabric", "1.20")

        fields["group"] shouldContainExactlyInAnyOrder listOf(
            FieldDefinition(
                path = listOf("mc", "1.20", "group"),
                value = "g",
            ),
            FieldDefinition(
                path = listOf("loader", "fabric", "group"),
                value = "g",
            ),
        )
    }
}