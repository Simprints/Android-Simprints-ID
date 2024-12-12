package com.simprints.feature.logincheck

enum class LoginCheckError {
    // Sign in check errors
    DIFFERENT_PROJECT_ID,
    PROJECT_PAUSED,
    PROJECT_ENDING,

    // Login errors
    MISSING_GOOGLE_PLAY_SERVICES,
    GOOGLE_PLAY_SERVICES_OUTDATED,
    INTEGRITY_SERVICE_ERROR,
    MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
    UNEXPECTED_LOGIN_ERROR,

    // Other errores
    ROOTED_DEVICE,
}
