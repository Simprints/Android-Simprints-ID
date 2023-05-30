package com.simprints.fingerprint.exceptions.unexpected

import com.simprints.fingerprint.exceptions.FingerprintSimprintsException

open class FingerprintUnexpectedException : FingerprintSimprintsException {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
