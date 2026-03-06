package net.xolt.freecam.publish.logger

import io.kotest.matchers.comparables.shouldBeLessThan
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
}