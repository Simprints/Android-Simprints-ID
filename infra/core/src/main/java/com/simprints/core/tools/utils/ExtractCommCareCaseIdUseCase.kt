package com.simprints.core.tools.utils

import com.simprints.infra.serialization.SimJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class ExtractCommCareCaseIdUseCase @Inject constructor() {
    operator fun invoke(metadata: String?): String? = metadata?.takeUnless { it.isBlank() }?.let {
        try {
            SimJson
                .decodeFromString<Map<String, JsonElement>>(it)[ARG_CASE_ID]
                ?.jsonPrimitive
                ?.takeIf { jsonPrimitive -> jsonPrimitive.isString }
                ?.content
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val ARG_CASE_ID = "caseId"
    }
}
