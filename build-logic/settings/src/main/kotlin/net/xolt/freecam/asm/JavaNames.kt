package net.xolt.freecam.asm

import org.gradle.kotlin.dsl.support.uppercaseFirstChar

/**
 * Convert a camelCase, kebab-case, or snake_case input to PascalCase.
 *
 * Does not sanitize for valid Java identifiers, see [toJavaIdentifier].
 */
internal fun String.toPascalCase(): String =
    split('-', '_', ' ', '\n').joinToString("") { part ->
        part.uppercaseFirstChar()
    }

/**
 * Sanitize an input string to a valid Java/JVM identifier.
 */
internal fun String.toJavaIdentifier(): String =
    mapIndexed { i, c ->
        when {
            i == 0 && !c.isJavaIdentifierStart() -> '_'
            !c.isJavaIdentifierPart() -> '_'
            else -> c
        }
    }.joinToString("")
