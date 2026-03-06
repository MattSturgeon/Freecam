package net.xolt.freecam.publish.logging

internal fun LogContext.errorsToStderr() {
    if (level <= LogLevel.ERROR) handler = System.err::println
}