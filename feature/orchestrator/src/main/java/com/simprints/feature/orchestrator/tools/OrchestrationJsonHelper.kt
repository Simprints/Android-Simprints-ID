package com.simprints.feature.orchestrator.tools

import com.simprints.feature.orchestrator.steps.orchestratorSerializersModule
import com.simprints.infra.serialization.SimJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OrchestrationJsonHelper @Inject constructor() {
    private val json: Json by lazy {
        Json(from = SimJson) {
            serializersModule = SimJson.serializersModule + orchestratorSerializersModule
        }
    }

    inline fun <reified T> encodeToString(value: T): String = json.encodeToString(value)

    inline fun <reified T> decodeFromString(string: String): T = json.decodeFromString(string)
}
