package net.xolt.freecam.publish.logging

import kotlin.test.BeforeTest

abstract class AbstractLoggerTest(
    private val level: LogLevel = LogLevel.QUIET,
) {

    lateinit var logger: Logger

    private val logStore = mutableListOf<String>()
    val logs: List<String> get() = logStore

    @BeforeTest
    fun setup() {
        logger = Logger(parent = null, level = level).apply {
            decorate { handler = logStore::add }
        }
        logStore.clear()
    }
}