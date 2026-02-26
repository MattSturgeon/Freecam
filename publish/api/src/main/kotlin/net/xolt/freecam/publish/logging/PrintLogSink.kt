package net.xolt.freecam.publish.logging

/**
 * A [LogSink] that prints to stdout or stderr.
 *
 * Follows Unix CLI conventions:
 *   - stdout → normal user-facing output
 *   - stderr → diagnostics (warnings, errors, debug, trace)
 */
val PrintLogSink: LogSink = { message ->
    level.output.println(message)
}

val LogLevel.isStderr: Boolean
    get() = when (this) {
        LogLevel.ERROR,
        LogLevel.WARNING,
        LogLevel.DEBUG,
        LogLevel.TRACE -> true
        LogLevel.INFO,
        LogLevel.NONE -> false
    }

internal val LogLevel.output
    get() = if (isStderr) System.err else System.out
