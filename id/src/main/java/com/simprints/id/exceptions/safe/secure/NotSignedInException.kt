package com.simprints.id.exceptions.safe.secure

import com.simprints.id.exceptions.safe.SafeException

class NotSignedInException(message: String = "NotSignedInException")
    : SafeException(message)
