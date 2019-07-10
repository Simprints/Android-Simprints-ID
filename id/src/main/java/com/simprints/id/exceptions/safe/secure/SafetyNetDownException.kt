package com.simprints.id.exceptions.safe.secure

import com.simprints.id.exceptions.safe.SafeException

class SafetyNetDownException(message: String = "Safety net down exception", val reason: SafetyNetErrorReason) : SafeException(message)

enum class SafetyNetErrorReason {
    SAFETYNET_DOWN,
    SAFETYNET_ERROR
}
