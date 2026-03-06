package net.xolt.freecam.publish.logger

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class AbstractLoggerTest(
    private val level: LogLevel = LogLevel.QUIET,
) {

    private lateinit var initialLogLevel: LogLevel
    private lateinit var initialDecorators: List<LogContext.() -> Unit>

    private val _messages = mutableListOf<String>()
    val messages: List<String> get() = _messages

    @BeforeTest
    fun setup() {
        initialLogLevel = Logger.level
        initialDecorators = Logger.decorators
        Logger.level = level
        Logger.decorators = emptyList()
        Logger.decorate {
            handler = _messages::add
        }
        _messages.clear()
    }

    @AfterTest
    fun cleanup() {
        Logger.level = initialLogLevel
        Logger.decorators = initialDecorators
    }
}