package net.xolt.freecam.publish.logging

enum class LogLevel {
    NONE,
    ERROR,
    WARNING,
    INFO,
    DEBUG,
    TRACE,
}

operator fun LogLevel.plus(increment: Int) = shift(increment)
operator fun LogLevel.minus(decrement: Int) = shift(-decrement)

private fun LogLevel.shift(delta: Int): LogLevel = LogLevel.entries[
    (ordinal + delta).coerceIn(0, LogLevel.entries.lastIndex)
]
