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

    // Sign in check errors
    DIFFERENT_PROJECT_ID,
    PROJECT_PAUSED,
    PROJECT_ENDING,

    // Other errores
    ROOTED_DEVICE,
    ;

}
