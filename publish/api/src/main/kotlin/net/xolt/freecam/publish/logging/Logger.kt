package net.xolt.freecam.publish.logging

import net.xolt.freecam.publish.logging.Logger.Companion.default


abstract class Logger {

    /** A prefix added to messages logged by this logger. */
    abstract val prefix: String

    /**
     * The log level threshold.
     *
     * - Logs at this level, or a 'less verbose' level, are evaluated.
     * - Logs at a 'more verbose' level are skipped.
     *
     * Usually defaults to [LogLevel.INFO].
     * In which case, [info], [warn], and [error] logs are evaluated,
     * while [debug] and [trace] logs are not.
     *
     * @sample samples.LoggerSamples.levelUsage
     */
    abstract var level: LogLevel

    /**
     * Whether a message with this log level would currently be printed.
     *
     * [LogLevel.NONE] is never enabled.
     * Other levels are enabled unless they are more verbose than [level].
     */
    inline val LogLevel.enabled
        get() = this@Logger.level >= this && this > LogLevel.NONE

    /**
     * The sink that will be logged to.
     *
     * Usually defaults to [PrintLogSink].
     */
    abstract var sink: LogSink

    /** Functions that decorate messages before they ase logged. */
    private val decorators = mutableListOf<LogDecorator>()

    /**
     * Install a [decorator][LogDecorator] that can modify log messages.
     *
     * The last decorator installed runs first (LIFO/FILO order).
     */
    fun decorate(decorator: LogDecorator) {
        decorators.add(0, decorator)
    }

    /** Evaluates and logs [msg] if [LogLevel.ERROR] is [enabled]. */
    inline fun error(msg: () -> String) = log(LogLevel.ERROR, msg)

    /** Evaluates and logs [msg] if [LogLevel.WARNING] is [enabled]. */
    inline fun warn(msg: () -> String) = log(LogLevel.WARNING, msg)

    /** Evaluates and logs [msg] if [LogLevel.INFO] is [enabled]. */
    inline fun info(msg: () -> String) = log(LogLevel.INFO, msg)

    /** Evaluates and logs [msg] if [LogLevel.DEBUG] is [enabled]. */
    inline fun debug(msg: () -> String) = log(LogLevel.DEBUG, msg)

    /** Evaluates and logs [msg] if [LogLevel.TRACE] is [enabled]. */
    inline fun trace(msg: () -> String) = log(LogLevel.TRACE, msg)

    /** Evaluates and logs [msg] if [level] is [enabled]. */
    inline fun log(level: LogLevel = LogLevel.INFO, msg: () -> String) {
        if (level.enabled) {
            @Suppress("DEPRECATION")
            LogContext(level).log(msg())
        }
    }

    /**
     * Applies [decorators][decorate] and [prefix], then logs to [sink].
     *
     * @param message the message to be logged.
     *
     * @receiver the log message's context
     * @param message the undecorated message
     * @see [Logger.log]
     */
    @Deprecated(
        message = "Public only so that Logger.log() can be inline",
        replaceWith = ReplaceWith("log(level) { message }"),
    )
    fun LogContext.log(message: String) {
        sink(applyPrefix(decorate(this, message)))
    }

    /** Decorate a [message] using this logger's [decorators] in the given [context][ctx]. */
    internal open fun decorate(ctx: LogContext, message: String) =
        decorators.fold(message) { msg, decorate ->
            ctx.decorate(msg)
        }

    /** Apply [prefix] to [message] and normalise linebreaks. */
    internal fun applyPrefix(message: String): String {
        return if (prefix.isEmpty()) message
        else message.lineSequence().joinToString(
            prefix = prefix,
            separator = indentedLinebreak,
        )
    }

    private val indentedLinebreak by lazy {
        "\n" + " ".repeat(prefix.length)
    }

    /**
     * Create a scoped view of this logger.
     *
     * @param scope name of the scope, used in log prefix. E.g. `[scope]`
     * @param config optional configuration block applied to the scoped logger.
     */
    fun scoped(scope: String, config: Logger.() -> Unit = { }): Logger =
        ScopedLogger(this, scope).apply(config)

    companion object {

        /** Default logger. */
        val default: Logger = DefaultLogger()

        /** Configure the [default] logger. */
        fun configure(config: Logger.() -> Unit) {
            default.apply(config)
        }

        /**
         * Create a scoped view of the [default] logger.
         *
         * @param scope name of the scope, used in log prefix. E.g. `[scope]`
         * @param config optional configuration block applied to the scoped logger.
         */
        fun scoped(scope: String, config: Logger.() -> Unit = { }): Logger =
            default.scoped(scope, config)
    }
}
