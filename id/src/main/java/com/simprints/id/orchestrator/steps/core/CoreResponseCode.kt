package com.simprints.id.orchestrator.steps.core

private const val CORE_RESPONSE_CODE = 400

enum class CoreResponseCode(val value: Int) {
    CONSENT(CORE_RESPONSE_CODE + 1),
    CORE_EXIT_FORM(CORE_RESPONSE_CODE + 2),
    FINGERPRINT_EXIT_FORM(CORE_RESPONSE_CODE + 3),
    FACE_EXIT_FORM(CORE_RESPONSE_CODE + 4),
    FETCH_GUID(CORE_RESPONSE_CODE + 5),
    ERROR(CORE_RESPONSE_CODE + 6)
}
