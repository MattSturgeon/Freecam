package net.xolt.freecam.publish.logging

internal class DefaultLogger(
    override val prefix: String = "",
    @Volatile
    override var level: LogLevel = LogLevel.INFO,
    @Volatile
    override var sink: LogSink = PrintLogSink,
) : Logger()