package com.simprints.core.tools.utils

import com.simprints.core.tools.json.JsonHelper
import javax.inject.Inject

class ExtractCommCareCaseIdUseCase @Inject constructor() {
    operator fun invoke(metadata: String?): String? = metadata?.takeUnless { it.isBlank() }?.let {
        try {
            JsonHelper.fromJson<Map<String, Any>>(it)[ARG_CASE_ID] as? String
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val ARG_CASE_ID = "caseId"
    }
}
