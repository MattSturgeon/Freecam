package net.xolt.freecam.publish.logging

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GithubAnnotationsLogDecoratorTest {

    @Test
    fun `error message gets error annotation`() {
        val ctx = LogContext(LogLevel.ERROR)

        val result = ctx.GitHubAnnotationsLogDecorator("failure")

        result shouldBe "::error::failure"
    }

    @Test
    fun `debug message gets debug annotation`() {
        val ctx = LogContext(LogLevel.DEBUG)

        val result = ctx.GitHubAnnotationsLogDecorator("debug info")

        result shouldBe "::debug::debug info"
    }

    @Test
    fun `normal message unchanged`() {
        val ctx = LogContext(LogLevel.INFO)

        val result = ctx.GitHubAnnotationsLogDecorator("hello")

        result shouldBe "hello"
    }
}