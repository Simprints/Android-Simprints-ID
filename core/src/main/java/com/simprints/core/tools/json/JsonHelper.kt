package com.simprints.core.tools.json

import androidx.annotation.Keep
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

@Keep
class JsonHelper {

    val jackson: ObjectMapper by lazy {
        ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun toJson(any: Any): String {
        return jackson.writeValueAsString(any)
    }

    inline fun <reified T> fromJson(json: String, type: TypeReference<T>): T {
        return jackson.readValue(json, type)
    }

    inline fun <reified T> fromJson(json: String): T {
        return jackson.readValue(json, T::class.java)
    }
}
