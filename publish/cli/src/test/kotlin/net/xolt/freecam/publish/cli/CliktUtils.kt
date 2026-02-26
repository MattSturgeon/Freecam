package net.xolt.freecam.publish.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.parsers.CommandLineParser

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * Unlike [parse], does not run the command's `run` method, so it can be used to test argument parsing and validation without side effects.
 */
fun SuspendingCliktCommand.parseWithoutRunning(argv: List<String>) =
    CommandLineParser.parseAndRun(this, argv) { /* no-op */ }
