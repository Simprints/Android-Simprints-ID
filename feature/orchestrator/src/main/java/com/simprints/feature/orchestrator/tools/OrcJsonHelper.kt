package com.simprints.feature.orchestrator.tools

import com.simprints.feature.orchestrator.steps.orchestratorSerializersModule
import kotlinx.serialization.json.Json

object OrcJsonHelper {
    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            coerceInputValues = true
            serializersModule = orchestratorSerializersModule
        }
    }
}
