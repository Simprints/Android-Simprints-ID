package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException

class OtaFailedException(
    message: String = "OtaFailedException",
    cause: Throwable? = null
) : FingerprintSafeException(message, cause)
