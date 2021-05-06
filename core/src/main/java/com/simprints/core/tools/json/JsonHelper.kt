package com.simprints.core.tools.json

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import timber.log.Timber

object JsonHelper {

    val jackson: ObjectMapper by lazy {
        ObjectMapper()
            .registerKotlinModule()
            .setSerializationInclusion(Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun toJson(any: Any): String {
        val startTime = System.currentTimeMillis()
        val rtn = jackson.writeValueAsString(any)
        val endTime = System.currentTimeMillis()
        Timber.v("JACKSON SPEED: serialize json object = ${endTime - startTime}")
        return rtn
    }

    inline fun <reified T> fromJson(json: String, type: TypeReference<T>): T {
        val startTime = System.currentTimeMillis()
        val rtn = jackson.readValue(json, type)
        val endTime = System.currentTimeMillis()
        Timber.d("JACKSON SPEED: fromJson type ref = ${endTime - startTime}")
        return rtn
    }

    inline fun <reified T> fromJson(json: String): T {
        val startTime = System.currentTimeMillis()
        val rtn = jackson.readValue(json, T::class.java)
        val endTime = System.currentTimeMillis()
        Timber.d("JACKSON SPEED: fromJson = ${endTime - startTime}")
        return rtn
    }

    fun validateJsonOrThrow(json: String) {
        val startTime = System.currentTimeMillis()
        jackson.readTree(json)
        val endTime = System.currentTimeMillis()
        Timber.d("JACKSON SPEED: validate or throw = ${endTime - startTime}")
    }
}
