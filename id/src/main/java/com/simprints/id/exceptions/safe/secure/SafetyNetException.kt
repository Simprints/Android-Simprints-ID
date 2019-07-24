package com.simprints.id.exceptions.safe.secure

import com.simprints.id.exceptions.safe.SafeException

class SafetyNetException(message: String = "Safety net down exception", val reason: SafetyNetExceptionReason) : SafeException(message)

enum class SafetyNetExceptionReason {
    SERVICE_UNAVAILABLE,
    INVALID_CLAIMS
}
