package com.simprints.core.tools.json

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonHelper {

    val jackson: ObjectMapper by lazy {
        ObjectMapper()
            .registerKotlinModule()
            .setSerializationInclusion(Include.NON_NULL)
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

    fun validateJsonOrThrow(json: String) {
        jackson.readTree(json)
    }
}
