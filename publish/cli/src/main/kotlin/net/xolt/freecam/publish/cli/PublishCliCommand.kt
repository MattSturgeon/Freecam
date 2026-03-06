package net.xolt.freecam.publish.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
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
    val verbosityLevel: LogLevel get() = verbosity.level

    // TODO: use a (hidden?) --interactive or --output-format option
    private val interactive
        get() = System.console() != null

    // TODO: use a (hidden?) --gha-annotations flag, or integrate with --output-format
    private val gha
        get() = System.getenv("GITHUB_ACTIONS") == "true"

    override suspend fun run() {
        logger.apply {
            level = verbosityLevel
            if (gha) decorate { githubAnnotations() }
            if (interactive) decorate { ansiColors() }
        }

        publisher.use { it.publish(metadata) }
    }
}
