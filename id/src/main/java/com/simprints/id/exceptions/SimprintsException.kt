package com.simprints.id.exceptions


open class SimprintsException : RuntimeException {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
