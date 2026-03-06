package net.xolt.freecam.publish.logger

object Logger {

    @Volatile
    var level: LogLevel = LogLevel.NORMAL

    var decorators: List<LogContext.() -> Unit> = listOf(LogContext::errorsToStderr)
        private set

    fun decorate(decorator: LogContext.() -> Unit) {
        decorators += decorator
    }

    inline fun error(msg: () -> String) = log(LogLevel.ERROR, msg)
    inline fun info(msg: () -> String) = log(LogLevel.NORMAL, msg)
    inline fun verbose(msg: () -> String) = log(LogLevel.VERBOSE, msg)
    inline fun debug(msg: () -> String) = log(LogLevel.DEBUG, msg)

    inline fun log(level: LogLevel, msg: () -> String) {
        if (logs(level)) {
            LogContext(level, msg()).apply {
                for (decorator in decorators) {
                    decorator()
                }
                handler(message)
            }
        }
    }

    fun logs(level: LogLevel): Boolean =
        this.level >= level && this.level > LogLevel.QUIET
}

private fun LogContext.errorsToStderr() {
    if (level <= LogLevel.ERROR) handler = System.err::println
}
