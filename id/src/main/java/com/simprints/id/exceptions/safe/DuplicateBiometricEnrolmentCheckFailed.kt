package com.simprints.id.exceptions.safe

import com.simprints.core.exceptions.SafeException

class DuplicateBiometricEnrolmentCheckFailed(
    message: String = "There is a subject with confidence score above the high confidence level"
) : SafeException(message)
