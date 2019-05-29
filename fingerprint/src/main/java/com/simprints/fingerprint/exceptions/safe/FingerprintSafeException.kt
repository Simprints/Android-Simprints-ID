package com.simprints.fingerprint.exceptions.safe

import com.simprints.fingerprint.exceptions.FingerprintSimprintsException

open class FingerprintSafeException : FingerprintSimprintsException {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
