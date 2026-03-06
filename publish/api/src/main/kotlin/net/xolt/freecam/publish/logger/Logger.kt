package net.xolt.freecam.publish.logger

object Logger {
    @Volatile
    var level: LogLevel = LogLevel.NORMAL

    // TODO: add decorator system, maybe replacing print-handler DI with it

    var messageHandler: (String) -> Unit = System.out::println
        internal set

    var errorHandler: (String) -> Unit = System.err::println
        internal set

    inline fun error(msg: () -> String) = log(LogLevel.ERROR, msg)
    inline fun info(msg: () -> String) = log(LogLevel.NORMAL, msg)
    inline fun verbose(msg: () -> String) = log(LogLevel.VERBOSE, msg)
    inline fun debug(msg: () -> String) = log(LogLevel.DEBUG, msg)

    inline fun log(level: LogLevel, msg: () -> String) {
        if (logs(level)) {
            if (level <= LogLevel.ERROR) errorHandler(msg())
            else messageHandler(msg())
        }
    }

    fun logs(level: LogLevel): Boolean =
        this.level >= level && this.level > LogLevel.QUIET
}
