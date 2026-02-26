package net.xolt.freecam.publish.logging

class TestLogger(
    override val prefix: String = "",
    override var level: LogLevel = LogLevel.NONE,
) : Logger() {

    val logs: List<Pair<LogLevel, String>>
        get() = store.map { (ctx, msg) ->
            ctx.level to msg
        }

    val messages: List<String>
        get() = store.map { it.second }

    val logsWithContext: List<Pair<LogContext, String>>
        get() = store.toList()


    private val store = mutableListOf<Pair<LogContext, String>>()

    private val _sink: LogSink = { msg ->
        store += this to msg
    }

    override var sink: LogSink
        get() = _sink
        set(value) = error("Attempted to mutate TestLogger.sink")
}