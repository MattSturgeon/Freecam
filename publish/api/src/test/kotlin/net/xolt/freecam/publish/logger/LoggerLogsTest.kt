package net.xolt.freecam.publish.logger

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LoggerLogsTest : AbstractLoggerTest() {

    @Test
    fun `logs returns false when quiet`() {
        Logger.level = LogLevel.QUIET
        LogLevel.entries.forEach {
            Logger.logs(it) shouldBe false
        }
    }

    @Test
    fun `logs returns true for allowed level`() {
        Logger.level = LogLevel.NORMAL

        Logger.logs(LogLevel.ERROR) shouldBe true
        Logger.logs(LogLevel.NORMAL) shouldBe true
    }

    @Test
    fun `logs returns false for higher verbosity`() {
        Logger.level = LogLevel.NORMAL

        Logger.logs(LogLevel.VERBOSE) shouldBe false
        Logger.logs(LogLevel.DEBUG) shouldBe false
    }
}
