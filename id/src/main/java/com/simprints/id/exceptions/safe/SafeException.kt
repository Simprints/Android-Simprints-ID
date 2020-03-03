package com.simprints.id.exceptions.safe

open class SafeException : Throwable {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
