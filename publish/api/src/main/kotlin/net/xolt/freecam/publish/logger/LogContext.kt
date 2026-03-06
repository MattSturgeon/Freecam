package net.xolt.freecam.publish.logger

class LogContext(
    val level: LogLevel,
    var message: String,
    var handler: (String) -> Unit = System.out::println,
)