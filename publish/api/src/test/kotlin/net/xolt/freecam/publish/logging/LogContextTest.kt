package net.xolt.freecam.publish.logging

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LogContextTest {

    @Test
    fun `error level switches handler to stderr`() {
        val ctx = LogContext(LogLevel.ERROR, "msg")

        ctx.handler shouldBe System.err::println
    }

    @Test
    fun `non error level uses stdout`() {
        val ctx = LogContext(LogLevel.INFO, "msg")

        ctx.handler shouldBe System.out::println
    }
}
