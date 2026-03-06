package net.xolt.freecam.publish.cli

import com.github.ajalt.mordant.rendering.TextColors
import net.xolt.freecam.publish.logger.LogContext
import net.xolt.freecam.publish.logger.LogLevel

/**
 * Annotate log messages with [GitHub Actions annotations](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-commands).
 */
fun LogContext.githubAnnotations() {
    message = when (level) {
        LogLevel.ERROR -> "::error::$message"
        LogLevel.DEBUG -> "::debug::$message"
        // TODO: ::notice::, ::warning:: ?
        else -> message
    }
}

/**
 * Format log messages using ANSI colors
 * ([ECMA-48](https://www.ecma-international.org/wp-content/uploads/ECMA-48_5th_edition_june_1991.pdf) Select Graphic Rendition).
 */
fun LogContext.ansiColours() {
    message = when (level) {
        LogLevel.ERROR -> TextColors.red(message)
        LogLevel.DEBUG -> TextColors.gray(message)
        else -> message
    }
}
