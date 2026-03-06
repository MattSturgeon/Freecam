package net.xolt.freecam.publish.logger

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LoggerTest : AbstractLoggerTest() {

    @Test
    fun `message is evaluated lazily`() {
        Logger.level = LogLevel.ERROR

        var evaluated = false

        Logger.debug {
            evaluated = true
            "debug"
        }

        evaluated.shouldBeFalse()
    }

    @Test
    fun `info logs when level is NORMAL`() {
        Logger.level = LogLevel.NORMAL

        Logger.info { "hello" }

        messages shouldContainExactly listOf("hello")
    }

    @Test
    fun `debug does not log when level is NORMAL`() {
        Logger.level = LogLevel.NORMAL

        Logger.debug { "debug message" }

        messages shouldBe emptyList()
    }

    @Test
    fun `debug logs when level is DEBUG`() {
        Logger.level = LogLevel.DEBUG

        Logger.debug { "debug message" }

        messages shouldContainExactly listOf("debug message")
    }

    @Test
    fun `quiet disables all logging`() {
        Logger.level = LogLevel.QUIET

        Logger.error { "error" }
        Logger.info { "info" }
        Logger.debug { "debug" }

        messages shouldBe emptyList()
    }

    @Test
    fun `decorators run in FILO order`() {
        Logger.level = LogLevel.NORMAL

        Logger.decorate { message = "A$message" }
        Logger.decorate { message = "B$message" }
        Logger.decorate { message = "C$message" }

        Logger.info { "msg" }

        messages shouldBe listOf("ABCmsg")
    }
}
