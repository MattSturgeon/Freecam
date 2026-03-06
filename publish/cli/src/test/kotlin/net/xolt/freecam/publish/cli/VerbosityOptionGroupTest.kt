package net.xolt.freecam.publish.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.testing.test
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import net.xolt.freecam.publish.logger.LogLevel
import net.xolt.freecam.publish.logger.plus
import kotlin.test.Test

class VerbosityOptionGroupTest {

    @Test
    fun `get expected verbosity from various args`() {
        listOf(
            TestFixture("NORMAL is default", LogLevel.NORMAL),
            TestFixture("--quiet is QUIET", LogLevel.QUIET, "--quiet"),
            TestFixture("--verbosity=quiet is QUIET", LogLevel.QUIET, "--verbosity=quiet"),
            TestFixture("QUIET is not incremented", LogLevel.QUIET, "--verbosity=quiet", "-vvv"),
            TestFixture("--quiet overrides --verbosity", LogLevel.QUIET, "--quiet", "--verbosity=ERROR"),
            TestFixture("NORMAL is incremented", LogLevel.NORMAL + 3, "--verbosity=NORMAL", "-vvv"),
            TestFixture("ERROR is incremented", LogLevel.ERROR + 3, "--verbosity=ERROR", "-vv", "--verbose"),
            TestFixture("Extra increments are ignored", LogLevel.entries.last(), "--verbosity=DEBUG", "-vvvvvv"),
        ).assertSoftly {
            val cmd = TestCommand()
            val result = cmd.test(*args)
            result.statusCode shouldBe 0
            cmd.verbosity shouldBe expected
        }
    }

    private class TestCommand : CliktCommand() {
        val options by VerbosityOptionGroup()
        val verbosity get() = options.level
        override fun run() = Unit
    }

    private class TestFixture(
        val clue: String,
        val expected: LogLevel,
        vararg val args: String,
    )

    private fun Iterable<TestFixture>.assertSoftly(block: TestFixture.() -> Unit) {
        io.kotest.assertions.assertSoftly {
            forEach {
                withClue(it.clue) {
                    it.block()
                }
            }
        }
    }
}
