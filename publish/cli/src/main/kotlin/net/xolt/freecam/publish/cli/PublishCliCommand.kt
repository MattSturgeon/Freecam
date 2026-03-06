package net.xolt.freecam.publish.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
import net.xolt.freecam.model.ReleaseMetadata
import net.xolt.freecam.publish.PublisherFactory
import net.xolt.freecam.publish.logging.LogLevel
import net.xolt.freecam.publish.logging.ansiColors
import net.xolt.freecam.publish.logging.githubAnnotations
import net.xolt.freecam.publish.logging.logger
import net.xolt.freecam.publish.model.GitHubConfig
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

internal class PublishCliCommand(
    version: String? = null,
    metadataSupplier: () -> ReleaseMetadata,
    publisherFactory: PublisherFactory,
) : SuspendingCliktCommand(name = "publish") {

    init {
        context {
            versionOption(version ?: metadata.modVersion)
            helpFormatter = {
                MordantHelpFormatter(
                    context = it,
                    requiredOptionMarker = null,
                    showDefaultValues = true,
                    showRequiredTag = true,
                )
            }
        }
    }

    private val publisher by lazy {
        publisherFactory.create(
            dryRun = dryRun,
            artifactsDir = artifactsDir,
            githubConfig = github,
        )
    }

    val metadata by lazy(metadataSupplier)

    val artifactsDir: Path by argument("artifacts-dir").path()
        .help("Directory containing the release artifacts")
        .validate {
            require(it.exists()) {
                "${it.absolute()} does not exist"
            }
            require(it.isDirectory()) {
                "${it.absolute()} is not a directory"
            }
        }

    val dryRun: Boolean by option("--dry-run").flag()
        .help("Perform a dry run without making any actual API calls")

    val github: GitHubConfig by GitHubOptionGroup()

    private val verbosity by VerbosityOptionGroup()
    val logLevel: LogLevel get() = verbosity.level

    val outputFormat by option("--output-format", hidden = true)
        .enum<OutputFormat> { it.toString().lowercase() }
        .defaultLazy(defaultForHelp = "automatic") {
            if (System.getenv("GITHUB_ACTIONS") == "true") OutputFormat.GHA
            // TODO: smarter ANSI support detection
            else if (System.console() != null) OutputFormat.ANSI
            else OutputFormat.PLAIN
        }

    override suspend fun run() {
        logger.level = logLevel
        when (outputFormat) {
            OutputFormat.ANSI -> logger.decorate { ansiColors() }
            OutputFormat.GHA -> logger.decorate { githubAnnotations() }
            OutputFormat.PLAIN -> { /* no-op */ }
        }

        publisher.use { it.publish(metadata) }
    }
}
