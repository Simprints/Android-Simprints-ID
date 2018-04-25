package com.simprints.id.exceptions.safe.secure

import com.simprints.id.exceptions.safe.SimprintsException


class NotSignedInException(message: String = "NotSignedInException", cause: Throwable? = null)
    : SimprintsException(message, cause)
