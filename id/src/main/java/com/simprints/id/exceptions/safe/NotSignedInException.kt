package com.simprints.id.exceptions.safe


class NotSignedInException(message: String = "NotSignedInException", cause: Throwable? = null)
    : SimprintsException(message, cause)
