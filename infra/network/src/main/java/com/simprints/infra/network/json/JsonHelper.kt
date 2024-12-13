package com.simprints.infra.network.json

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

internal object JsonHelper {
    val jackson: ObjectMapper by lazy {
        ObjectMapper()
            .registerKotlinModule()
            .setSerializationInclusion(Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    inline fun <reified T> fromJson(json: String): T = jackson.readValue(json, T::class.java)
}
