package net.xolt.freecam.publish.logging

class LogContext(
    val level: LogLevel,
    var message: String,
    var handler: (String) -> Unit = System.out::println,
)