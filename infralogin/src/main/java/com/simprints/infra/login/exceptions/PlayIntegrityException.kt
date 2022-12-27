package com.simprints.infra.login.exceptions

class PlayIntegrityException(
    message: String = "Safety net down exception",
    val reason: PlayIntegrityExceptionReason
) : RuntimeException(message) {

    enum class PlayIntegrityExceptionReason {
        SERVICE_UNAVAILABLE
    }
}
