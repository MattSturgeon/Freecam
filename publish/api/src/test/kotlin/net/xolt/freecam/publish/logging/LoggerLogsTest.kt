package net.xolt.freecam.publish.logging

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LoggerLogsTest : AbstractLoggerTest() {

    @Test
    fun `logs returns false when quiet`() {
        logger.level = LogLevel.NONE
        LogLevel.entries.forEach {
            logger.logs(it) shouldBe false
        }
    }

    @Test
    fun `logs returns true for allowed level`() {
        logger.level = LogLevel.INFO

        logger.logs(LogLevel.ERROR) shouldBe true
        logger.logs(LogLevel.INFO) shouldBe true
    }

    @Test
    fun `logs returns false for higher verbosity`() {
        logger.level = LogLevel.INFO

        logger.logs(LogLevel.DEBUG) shouldBe false
        logger.logs(LogLevel.TRACE) shouldBe false
    }
}
