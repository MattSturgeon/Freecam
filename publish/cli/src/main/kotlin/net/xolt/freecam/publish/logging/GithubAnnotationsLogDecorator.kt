package net.xolt.freecam.publish.logging

/**
 * Annotate log messages with [GitHub Actions annotations](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-commands).
 */
fun LogContext.githubAnnotations() {
    message = when (level) {
        LogLevel.ERROR -> "::error::$message"
        LogLevel.WARNING -> "::warning::$message"
        LogLevel.DEBUG,
        LogLevel.TRACE -> "::debug::$message"
        // TODO: ::notice:: maybe for INFO ?
        else -> message
    }
}