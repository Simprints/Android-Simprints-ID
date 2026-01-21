package com.simprints.core.tools.json

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object JsonHelper {
    /**
     * @throws SerializationException if the JSON is not valid
     */
    fun validateJsonOrThrow(jsonString: String) {
        json.parseToJsonElement(jsonString)
    }

    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            coerceInputValues = true
        }
    }
}
