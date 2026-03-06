package net.xolt.freecam.publish.logging

/**
 * Default logger.
 */
val logger: Logger = Logger(parent = null, level = LogLevel.NORMAL).apply {
    decorate { errorsToStderr() }
}

class Logger internal constructor(
    private val parent: Logger? = null,

    @Volatile
    var level: LogLevel = parent!!.level,
) {
    private val decoratorStore = mutableListOf<LogContext.() -> Unit>()

    val decorators: Iterable<LogContext.() -> Unit>
        get() = sequenceOf(
            parent?.decorators,
            decoratorStore.asReversed(),
        ).filterNotNull().flatten().asIterable()

    fun decorate(decorator: LogContext.() -> Unit) {
        decoratorStore += decorator
    }

    inline fun error(msg: () -> String) = log(LogLevel.ERROR, msg)
    inline fun info(msg: () -> String) = log(LogLevel.NORMAL, msg)
    inline fun verbose(msg: () -> String) = log(LogLevel.VERBOSE, msg)
    inline fun debug(msg: () -> String) = log(LogLevel.DEBUG, msg)

    inline fun log(level: LogLevel, msg: () -> String) {
        if (logs(level)) {
            LogContext(level, msg()).apply {
                for (decorate in decorators) {
                    decorate()
                }
                handler(message)
            }
        }
    }

    fun logs(level: LogLevel): Boolean =
        this.level >= level && this.level > LogLevel.QUIET
}
