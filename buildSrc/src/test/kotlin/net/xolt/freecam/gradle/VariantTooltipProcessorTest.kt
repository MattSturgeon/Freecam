package net.xolt.freecam.gradle

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class VariantTooltipProcessorTest {

    @TestFactory
    fun `Basic tests`(): List<DynamicTest> {
        val twoVariants = mapOf(
            "foo.@NormalTooltip" to "normal",
            "foo.@SpecialTooltip" to "special"
        )

        return listOf(
            processorTest(
                name = "Normal tooltip is used",
                processor = VariantTooltipTransformer("normal"),
                translations = twoVariants,
                result = mapOf("foo.@Tooltip" to "normal")
            ),
            processorTest(
                name = "Normal tooltip is removed",
                processor = VariantTooltipTransformer("other"),
                translations = twoVariants,
                result = emptyMap()
            ),
            processorTest(
                name = "Special tooltip is used",
                processor = VariantTooltipTransformer("special"),
                translations = twoVariants,
                result = mapOf("foo.@Tooltip" to "special")
            )
        )
    }

}