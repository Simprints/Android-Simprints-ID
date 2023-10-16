package com.simprints.fingerprint.infra.scanner.exceptions.safe

import com.simprints.fingerprint.infra.scanner.exceptions.ScannerException

open class ScannerSafeException(
    message: String,
    cause: Throwable? = null,
) : ScannerException(message, cause)
