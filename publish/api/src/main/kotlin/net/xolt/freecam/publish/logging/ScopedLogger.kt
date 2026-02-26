package net.xolt.freecam.publish.logging

internal class ScopedLogger(
    val parent: Logger,
    scope: String,
    @Volatile
    private var localLevel: LogLevel? = null,
    @Volatile
    private var localSink: LogSink? = null,
) : Logger() {

    override val prefix: String = buildString {
        append(parent.prefix)
        if (isNotEmpty() && !endsWith(' ')) {
            append(' ')
        }
        append('[')
        append(scope)
        append(']')
        append(' ')
    }

    override var level
        get() = localLevel?.coerceAtMost(parent.level) ?: parent.level
        set(level) { localLevel = level }

    override var sink
        get() = localSink ?: parent.sink
        set(value) { localSink = value }

    override fun decorate(ctx: LogContext, message: String): String {
        return super.decorate(ctx, parent.decorate(ctx, message))
    }
}