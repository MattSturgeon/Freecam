package samples

import net.xolt.freecam.publish.logging.LogLevel
import net.xolt.freecam.publish.logging.Logger

internal object LoggerSamples {

    fun levelUsage(logger: Logger) {
        logger.level = LogLevel.INFO

        // Logs at the same level
        logger.info { "info is printed" }

        // Logs at a lower verbosity level
        logger.warn { "warn is printed" }
        logger.error { "error is printed" }

        // Logs at a higher verbosity level
        logger.debug { "debug is NOT printed" }
        logger.trace { "trace is NOT printed" }
    }
}