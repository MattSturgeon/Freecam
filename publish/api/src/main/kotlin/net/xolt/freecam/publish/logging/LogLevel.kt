package net.xolt.freecam.publish.logging

enum class LogLevel {
    /** Silent. */
    NONE,
    /** Fatal errors and unrecoverable issues. */
    ERROR,
    /** Unexpected but recoverable issues. */
    WARNING,
    /** Normal operational output. */
    INFO,
    /** Useful debugging output. */
    DEBUG,
    /** Verbose diagnostic data. */
    TRACE,
}

operator fun LogLevel.plus(increment: Int) = shift(increment)
operator fun LogLevel.minus(decrement: Int) = shift(-decrement)

private fun LogLevel.shift(delta: Int): LogLevel = LogLevel.entries[
    (ordinal + delta).coerceIn(0, LogLevel.entries.lastIndex)
]
