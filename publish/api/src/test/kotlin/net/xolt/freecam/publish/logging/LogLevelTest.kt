package net.xolt.freecam.publish.logging

import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LogLevelTest {
    @Test
    fun `QUIET is less than ERROR`() {
        LogLevel.QUIET shouldBeLessThan LogLevel.ERROR
    }

    @Test
    fun `ERROR is less than NORMAL`() {
        LogLevel.ERROR shouldBeLessThan LogLevel.NORMAL
    }

    @Test
    fun `NORMAL is less than VERBOSE`() {
        LogLevel.NORMAL shouldBeLessThan LogLevel.VERBOSE
    }

    @Test
    fun `VERBOSE is less than DEBUG`() {
        LogLevel.VERBOSE shouldBeLessThan  LogLevel.DEBUG
    }

    @Test
    fun `plus increments level`() {
        (LogLevel.ERROR + 1) shouldBe LogLevel.NORMAL
    }

    @Test
    fun `minus decrements level`() {
        (LogLevel.VERBOSE - 1) shouldBe LogLevel.NORMAL
    }

    @Test
    fun `plus clamps to max`() {
        (LogLevel.DEBUG + 5) shouldBe LogLevel.DEBUG
    }

    @Test
    fun `minus clamps to min`() {
        (LogLevel.QUIET - 10) shouldBe LogLevel.QUIET
    }
}