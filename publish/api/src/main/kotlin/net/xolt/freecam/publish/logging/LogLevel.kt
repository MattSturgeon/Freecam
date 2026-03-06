package net.xolt.freecam.publish.logging

enum class LogLevel {
    QUIET,
    ERROR,
    NORMAL,
    VERBOSE,
    DEBUG,
}

operator fun LogLevel.plus(increment: Int) = shift(increment)
operator fun LogLevel.minus(decrement: Int) = shift(-decrement)

private fun LogLevel.shift(delta: Int): LogLevel = LogLevel.entries[
    (ordinal + delta).coerceIn(0, LogLevel.entries.lastIndex)
]
