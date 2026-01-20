package com.simprints.core.tools.extentions

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun Map<String, JsonElement?>.toStringMap(): Map<String, String> = mapValues { (_, jsonElement) ->
    when (jsonElement) {
        is JsonPrimitive -> jsonElement.content
        null -> ""
        else -> jsonElement.toString()
    }
}

fun Map<String, Any?>.toJsonElementMap(): Map<String, JsonElement?> = mapValues { (_, value) ->
    when (value) {
        is String -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        null -> JsonPrimitive("")
        else -> JsonPrimitive(value.toString())
    }
}
