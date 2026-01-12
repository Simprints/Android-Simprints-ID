package com.simprints.infra.network.json

import kotlinx.serialization.json.Json

internal class JsonHelper {
    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            coerceInputValues = true
        }
    }

    inline fun <reified T> fromJson(jsonString: String): T = json.decodeFromString(jsonString)

    inline fun <reified T> toJson(obj: T): String = json.encodeToString(obj)
}
