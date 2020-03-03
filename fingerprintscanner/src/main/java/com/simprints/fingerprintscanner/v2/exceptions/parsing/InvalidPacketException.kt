package com.simprints.fingerprintscanner.v2.exceptions.parsing

class InvalidPacketException(
    message: String? = null,
    cause: Throwable? = null
) : InvalidMessageException(message, cause)
