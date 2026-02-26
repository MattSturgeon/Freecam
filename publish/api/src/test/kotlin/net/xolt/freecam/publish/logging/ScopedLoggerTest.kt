package net.xolt.freecam.publish.logging

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlin.test.BeforeTest
import kotlin.test.Test

class ScopedLoggerTest {

    lateinit var logger: TestLogger

    @BeforeTest
    fun setup() {
        logger = TestLogger()
    }

    @Test
    fun `child decorators apply after parent's`() {
        logger.level = LogLevel.INFO
        val child = logger.scoped("child")

        logger.decorate { "A$it" }
        child.decorate { "a$it" }
        logger.decorate { "B$it" }
        logger.decorate { "C$it" }
        child.decorate { "b$it" }
        child.decorate { "c$it" }

        logger.info { "msg" }
        child.info { "childmsg" }

        logger.messages shouldContainExactly  listOf(
            "ABCmsg",
            "[child] abcABCchildmsg",
        )
    }

    @Test
    fun `newlines align with prefix`() {
        logger.level = LogLevel.INFO
        val child = logger.scoped("child")

        logger.info { "hello\nworld" }
        child.info { "hello\nworld!" }
        child.info { "many\nmany\nlines\n!" }

        logger.messages shouldContainExactly listOf(
            "hello\nworld",
            "[child] hello\n        world!",
            """
                [child] many
                        many
                        lines
                        !
            """.trimIndent(),
        )
    }

    @Test
    fun `less verbose child overrides more verbose parent`() {
        logger.level = LogLevel.INFO
        val child = logger.scoped("child") {
            level = LogLevel.WARNING
        }

        logger.level shouldBe LogLevel.INFO
        child.level shouldBe LogLevel.WARNING
    }

    @Test
    fun `less verbose parent overrides more verbose child`() {
        logger.level = LogLevel.INFO
        val child = logger.scoped("child") {
            level = LogLevel.DEBUG
        }

        logger.level shouldBe LogLevel.INFO
        child.level shouldBe LogLevel.INFO
    }

    @Test
    fun `child sink shadows parent's`() {
        val childSink: LogSink = { }
        val globalSink: LogSink = logger.sink
        val child = logger.scoped("child") {
            sink = childSink
        }

        logger.sink shouldNotBeSameInstanceAs child.sink
        logger.sink shouldBeSameInstanceAs globalSink
        child.sink shouldBeSameInstanceAs childSink
    }
}