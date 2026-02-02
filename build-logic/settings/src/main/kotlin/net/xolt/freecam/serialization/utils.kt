package net.xolt.freecam.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

private val json = Json

internal inline fun <reified T> String.decodeAs(): T =
    decodeUsing(serializer())

internal fun <T> String.decodeUsing(serializer: KSerializer<T>): T =
    json.decodeFromValue(serializer, this)

internal inline fun <reified T> Json.decodeFromValue(string: String): T =
    decodeFromValue(serializer(), string)

internal fun <T> Json.decodeFromValue(serializer: KSerializer<T>, value: String): T =
    decodeFromString(serializer, encodeToString(value))