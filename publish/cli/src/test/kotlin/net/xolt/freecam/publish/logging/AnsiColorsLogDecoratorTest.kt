package net.xolt.freecam.publish.logging

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContainOnlyOnce
import kotlin.test.Test

class AnsiColorsLogDecoratorTest {

    @Test
    fun `error message is coloured`() {
        val ctx = LogContext(LogLevel.ERROR)

        val result = ctx.AnsiColorLogDecorator("boom")

        result shouldNotBe "boom"
        result shouldContainOnlyOnce "boom"
    }

    @Test
    fun `trace message is coloured`() {
        val ctx = LogContext(LogLevel.TRACE)

        val result = ctx.AnsiColorLogDecorator("debug")

        result shouldNotBe "debug"
        result shouldContainOnlyOnce "debug"
    }

    @Test
    fun `info message unchanged`() {
        val ctx = LogContext(LogLevel.INFO)

        val result = ctx.AnsiColorLogDecorator("hello")

        result shouldBe "hello"
    }

}