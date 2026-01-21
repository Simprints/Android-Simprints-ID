package com.simprints.feature.orchestrator.tools

import com.simprints.feature.orchestrator.steps.orchestratorSerializersModule
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
internal class OrchestrationJsonHelper @Inject constructor() {
    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            coerceInputValues = true
            serializersModule = orchestratorSerializersModule
        }
    }

    inline fun <reified T> encodeToString(value: T): String = json.encodeToString(value)

    inline fun <reified T> decodeFromString(string: String): T = json.decodeFromString(string)
}
