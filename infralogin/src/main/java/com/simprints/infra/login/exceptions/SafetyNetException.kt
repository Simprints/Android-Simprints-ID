package com.simprints.infra.login.exceptions

class SafetyNetException(
    message: String = "Safety net down exception",
    val reason: SafetyNetExceptionReason
) : RuntimeException(message) {

    enum class SafetyNetExceptionReason {
        SERVICE_UNAVAILABLE,
        INVALID_CLAIMS
    }
}
