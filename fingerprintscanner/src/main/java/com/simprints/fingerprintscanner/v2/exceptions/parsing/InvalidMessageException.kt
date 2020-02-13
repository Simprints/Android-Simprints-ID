package com.simprints.fingerprintscanner.v2.exceptions.parsing

open class InvalidMessageException(
    message: String? = null,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)
