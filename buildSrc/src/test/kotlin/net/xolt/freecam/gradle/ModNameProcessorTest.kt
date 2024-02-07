package net.xolt.freecam.gradle

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class ModNameProcessorTest {

    @TestFactory
    fun `Basic tests`(): List<DynamicTest> {
        val sample1 = mapOf(
            "modid.name" to "ModName",
            "modid.name.special" to "(extra special)"
        )

        return listOf(
            processorTest(
                name = "Discard variant names",
                processor = ModNameTransformer("modid", "normal"),
                translations = sample1,
                result = mapOf(
                    "modid.name" to "ModName",
                    "modmenu.nameTranslation.modid" to "ModName"
                )
            ),
            processorTest(
                name = "Append \"extra special\" to name",
                processor = ModNameTransformer("modid", "special"),
                translations = sample1,
                result = mapOf(
                    "modid.name" to "ModName (extra special)",
                    "modmenu.nameTranslation.modid" to "ModName (extra special)"
                )
            )
        )
    }
}