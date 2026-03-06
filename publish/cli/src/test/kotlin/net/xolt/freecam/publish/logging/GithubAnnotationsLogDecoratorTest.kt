package net.xolt.freecam.publish.logging

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GithubAnnotationsLogDecoratorTest {

    @Test
    fun `error message gets error annotation`() {
        val ctx = LogContext(LogLevel.ERROR, "failure")

        ctx.githubAnnotations()

        ctx.message shouldBe "::error::failure"
    }

    @Test
    fun `debug message gets debug annotation`() {
        val ctx = LogContext(LogLevel.DEBUG, "debug info")

        ctx.githubAnnotations()

        ctx.message shouldBe "::debug::debug info"
    }

    @Test
    fun `normal message unchanged`() {
        val ctx = LogContext(LogLevel.INFO, "hello")

        ctx.githubAnnotations()

        ctx.message shouldBe "hello"
    }
}