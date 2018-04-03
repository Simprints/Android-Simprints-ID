package com.simprints.id.exceptions.safe

import com.simprints.id.exceptions.SimprintsThrowable


open class SimprintsException : RuntimeException, SimprintsThrowable {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
