package net.xolt.freecam.publish.logging

typealias LogSink = LogContext.(message: String) -> Unit

typealias LogDecorator = LogContext.(message: String) -> String

class LogContext(
    val level: LogLevel,
)
