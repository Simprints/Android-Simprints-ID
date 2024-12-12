package com.simprints.feature.clientapi.models

enum class ClientApiError {
    // Invalid request params errors
    INVALID_STATE_FOR_INTENT_ACTION,
    INVALID_METADATA,
    INVALID_MODULE_ID,
    INVALID_PROJECT_ID,
    INVALID_SELECTED_ID,
    INVALID_SESSION_ID,
    INVALID_USER_ID,
    INVALID_VERIFY_ID,
}
