package com.simprints.fingerprint.exceptions


open class FingerprintSimprintsException : RuntimeException {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
