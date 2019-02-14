package com.simprints.id.exceptions.unexpected

import com.simprints.id.exceptions.SimprintsException

open class UnexpectedException : SimprintsException {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
