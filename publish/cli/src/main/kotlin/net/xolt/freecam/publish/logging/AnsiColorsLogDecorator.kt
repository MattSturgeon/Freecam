package net.xolt.freecam.publish.logging

import com.github.ajalt.mordant.rendering.TextColors

/**
 * Format log messages using ANSI colors
 * ([ECMA-48](https://www.ecma-international.org/wp-content/uploads/ECMA-48_5th_edition_june_1991.pdf) Select Graphic Rendition).
 */
fun LogContext.ansiColors() {
    message = when (level) {
        LogLevel.ERROR -> TextColors.red(message)
        LogLevel.DEBUG -> TextColors.gray(message)
        else -> message
    }
}
