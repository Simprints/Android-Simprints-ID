package com.simprints.fingerprintscanner.v2.exceptions.ota

class InvalidFirmwareException(
    message: String? = null,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)
