package net.xolt.freecam.publish.logging

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LoggerTest : AbstractLoggerTest() {

    @Test
    fun `message is evaluated lazily`() {
        logger.level = LogLevel.ERROR

        var evaluated = false

        logger.debug {
            evaluated = true
            "debug"
        }

        evaluated.shouldBeFalse()
    }

    @Test
    fun `info logs when level is INFO`() {
        logger.level = LogLevel.INFO

        logger.info { "hello" }

        logs shouldContainExactly listOf("hello")
    }

    @Test
    fun `debug does not log when level is INFO`() {
        logger.level = LogLevel.INFO

        logger.debug { "debug message" }

        logs shouldBe emptyList()
    }

    @Test
    fun `debug logs when level is TRACE`() {
        logger.level = LogLevel.TRACE

        logger.debug { "debug message" }

        logs shouldContainExactly listOf("debug message")
    }

    @Test
    fun `NONE disables all logging`() {
        logger.level = LogLevel.NONE

        logger.error { "error" }
        logger.info { "info" }
        logger.debug { "debug" }

        logs shouldBe emptyList()
    }

    @Test
    fun `decorators run in FILO order`() {
        logger.level = LogLevel.INFO

        logger.decorate { message = "A$message" }
        logger.decorate { message = "B$message" }
        logger.decorate { message = "C$message" }

        logger.info { "msg" }

        logs shouldBe listOf("ABCmsg")
    }
}
