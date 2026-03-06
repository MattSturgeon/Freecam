package net.xolt.freecam.publish.logger

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ErrorsToStderrDecoratorTest {

    @Test
    fun `error level switches handler to stderr`() {
        val ctx = LogContext(LogLevel.ERROR, "msg")

        ctx.errorsToStderr()

        ctx.handler shouldBe System.err::println
    }

    @Test
    fun `non error level uses stdout`() {
        val ctx = LogContext(LogLevel.NORMAL, "msg")

        ctx.errorsToStderr()

        ctx.handler shouldBe System.out::println
    }
}
