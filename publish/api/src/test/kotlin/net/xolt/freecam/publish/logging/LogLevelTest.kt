package net.xolt.freecam.publish.logging

import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LogLevelTest {
    @Test
    fun `NONE is less than ERROR`() {
        LogLevel.NONE shouldBeLessThan LogLevel.ERROR
    }

    @Test
    fun `ERROR is less than INFO`() {
        LogLevel.ERROR shouldBeLessThan LogLevel.INFO
    }

    @Test
    fun `INFO is less than DEBUG`() {
        LogLevel.INFO shouldBeLessThan LogLevel.DEBUG
    }

    @Test
    fun `DEBUG is less than TRACE`() {
        LogLevel.DEBUG shouldBeLessThan  LogLevel.TRACE
    }

    @Test
    fun `plus increments level`() {
        (LogLevel.ERROR + 1) shouldBe LogLevel.WARNING
    }

    @Test
    fun `minus decrements level`() {
        (LogLevel.DEBUG - 1) shouldBe LogLevel.INFO
    }

    @Test
    fun `plus clamps to max`() {
        (LogLevel.TRACE + 5) shouldBe LogLevel.TRACE
    }

    @Test
    fun `minus clamps to min`() {
        (LogLevel.NONE - 10) shouldBe LogLevel.NONE
    }
}