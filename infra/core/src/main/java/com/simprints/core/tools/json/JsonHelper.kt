package com.simprints.core.tools.json

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonHelper {
    val jackson: ObjectMapper by lazy {
        ObjectMapper()
            .registerKotlinModule()
            .setSerializationInclusion(Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun addMixin(
        target: Class<*>,
        mixinSource: Class<*>,
    ) {
        jackson.addMixIn(target, mixinSource)
    }

    fun toJson(any: Any): String = jackson.writeValueAsString(any)

    fun toJson(
        any: Any,
        module: Module,
    ): String {
        val jackson = this.jackson.copy().apply {
            registerModule(module)
        }
        return jackson.writeValueAsString(any)
    }

    inline fun <reified T> fromJson(
        json: String,
        type: TypeReference<T>,
    ): T = jackson.readValue(json, type)

    inline fun <reified T> fromJson(
        json: String,
        module: Module,
        type: TypeReference<T>,
    ): T {
        val jackson = this.jackson.copy().apply {
            registerModule(module)
        }
        return jackson.readValue(json, type)
    }

    inline fun <reified T> fromJson(
        json: String,
        type: JavaType,
    ): T = jackson.readValue(json, type)

    inline fun <reified T> fromJson(json: String): T = jackson.readValue(json, T::class.java)

    fun validateJsonOrThrow(json: String) {
        jackson.readTree(json)
    }
}
