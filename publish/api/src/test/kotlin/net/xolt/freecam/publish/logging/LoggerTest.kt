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
    fun `info logs when level is NORMAL`() {
        logger.level = LogLevel.NORMAL

        logger.info { "hello" }

        logs shouldContainExactly listOf("hello")
    }

    @Test
    fun `debug does not log when level is NORMAL`() {
        logger.level = LogLevel.NORMAL

        logger.debug { "debug message" }

        logs shouldBe emptyList()
    }

    @Test
    fun `debug logs when level is DEBUG`() {
        logger.level = LogLevel.DEBUG

        logger.debug { "debug message" }

        logs shouldContainExactly listOf("debug message")
    }

    @Test
    fun `quiet disables all logging`() {
        logger.level = LogLevel.QUIET

        logger.error { "error" }
        logger.info { "info" }
        logger.debug { "debug" }

        logs shouldBe emptyList()
    }

    @Test
    fun `decorators run in FILO order`() {
        logger.level = LogLevel.NORMAL

        logger.decorate { message = "A$message" }
        logger.decorate { message = "B$message" }
        logger.decorate { message = "C$message" }

        logger.info { "msg" }

        logs shouldBe listOf("ABCmsg")
    }
}
