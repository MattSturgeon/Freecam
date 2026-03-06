package net.xolt.freecam.publish.logging

class LogContext(
    val level: LogLevel,
    var message: String,
    var handler: (String) -> Unit = level.output,
)

/**
 * Default output stream for each [LogLevel].
 *
 * Follows Unix CLI conventions:
 *   - stdout → normal user-facing output
 *   - stderr → diagnostics (warnings, errors, debug, trace)
 */
val LogLevel.output: (String) -> Unit
    get() = when (this) {
        LogLevel.ERROR,
        LogLevel.WARNING,
        LogLevel.DEBUG,
        LogLevel.TRACE -> System.err::println
        LogLevel.INFO -> System.out::println
        LogLevel.NONE -> { _: String -> /* no-op */ }
    }