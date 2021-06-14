package com.simprints.id.exceptions.safe.secure

import com.simprints.core.exceptions.SafeException

class NotSignedInException(message: String = "NotSignedInException")
    : SafeException(message)
