package net.xolt.freecam.gradle

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class ModDescriptionProcessorTest {

    @TestFactory
    fun `Basic tests`(): List<DynamicTest> {
        val sample1 = mapOf(
            "modid.description" to "Default description",
            "modid.description.special" to "(extra special)"
        )

        return listOf(
            processorTest(
                name = "Discard variant descriptions",
                processor = ModDescriptionTransformer("modid", "normal"),
                translations = sample1,
                result = mapOf(
                    "modid.description" to "Default description",
                    "modmenu.descriptionTranslation.modid" to "Default description",
                    "modmenu.summaryTranslation.modid" to "Default description"
                )
            ),
            processorTest(
                name = "Append \"extra special\" to description",
                processor = ModDescriptionTransformer("modid", "special"),
                translations = sample1,
                result = mapOf(
                    "modid.description" to "Default description (extra special)",
                    "modmenu.descriptionTranslation.modid" to "Default description (extra special)",
                    "modmenu.summaryTranslation.modid" to "Default description"
                )
            )
        )
    }
}