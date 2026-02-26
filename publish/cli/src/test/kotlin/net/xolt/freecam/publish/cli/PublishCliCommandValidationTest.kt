package net.xolt.freecam.publish.cli

import com.github.ajalt.clikt.core.UsageError
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.xolt.freecam.test.MetadataFixtures.testMetadata
import net.xolt.freecam.test.createTestFile
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.test.Test

class PublishCliCommandValidationTest {

    @Test
    fun `missing artifacts-dir fails validation`() {
        val metadata = testMetadata()
        val dir = Path("artifacts-dir")
        val cmd = testCommand(metadata = metadata)

        val ex = shouldThrow<UsageError> {
            cmd.parseWithoutRunning(
                listOf(
                    "--gh-token", "token",
                    "--gh-owner", "owner",
                    "--gh-repo", "repo",
                    "--git-sha", "committish",
                    dir.absolutePathString(),
                )
            )
        }

        ex.message shouldBe "${dir.absolute()} does not exist"
    }

    @Test
    fun `non-dir artifacts-dir fails validation`() {
        val metadata = testMetadata()

        val file = createTestFile()
        val cmd = testCommand(metadata = metadata)

        val ex = shouldThrow<UsageError> {
            cmd.parseWithoutRunning(
                listOf(
                    "--gh-token", "token",
                    "--gh-owner", "owner",
                    "--gh-repo", "repo",
                    "--git-sha", "committish",
                    file.absolutePathString(),
                )
            )
        }

        ex.message shouldBe "${file.absolute()} is not a directory"
    }
}
