package net.xolt.freecam.publish.logging

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContainOnlyOnce
import kotlin.test.Test

class AnsiColorsLogDecoratorTest {

    @Test
    fun `error message is coloured`() {
        val ctx = LogContext(LogLevel.ERROR, "boom")

        ctx.ansiColors()

        ctx.message shouldNotBe "boom"
        ctx.message shouldContainOnlyOnce "boom"
    }

    @Test
    fun `trace message is coloured`() {
        val ctx = LogContext(LogLevel.TRACE, "debug")

        ctx.ansiColors()

        ctx.message shouldNotBe "debug"
        ctx.message shouldContainOnlyOnce "debug"
    }

    @Test
    fun `info message unchanged`() {
        val ctx = LogContext(LogLevel.INFO, "hello")

        ctx.ansiColors()

        ctx.message shouldBe "hello"
    }

}