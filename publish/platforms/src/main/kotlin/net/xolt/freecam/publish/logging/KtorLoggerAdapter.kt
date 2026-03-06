package net.xolt.freecam.publish.logging

import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.LogLevel as KtorLogLevel
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal class KtorLoggerAdapter(private val level: LogLevel) : KtorLogger {
    override fun log(message: String) {
        logger.log(level) { message }
    }

    fun logs() = logger.logs(level)
}

fun LoggingConfig.useLoggingAdapter(level: LogLevel) {
    val adapter = KtorLoggerAdapter(level)
    this.logger = adapter
    this.level =
        if (adapter.logs()) level.toKtorLogLevel()
        else KtorLogLevel.NONE
}

fun LogLevel.toKtorLogLevel(): KtorLogLevel = when (this) {
    LogLevel.NONE,
    LogLevel.ERROR,
    LogLevel.WARNING,
    LogLevel.INFO -> KtorLogLevel.NONE
    LogLevel.DEBUG -> KtorLogLevel.INFO
    LogLevel.TRACE -> KtorLogLevel.HEADERS
}