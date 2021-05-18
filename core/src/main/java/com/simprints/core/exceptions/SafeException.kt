package com.simprints.core.exceptions

open class SafeException : SimprintsException {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
