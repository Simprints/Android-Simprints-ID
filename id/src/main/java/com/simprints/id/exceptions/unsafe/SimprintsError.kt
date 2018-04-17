package com.simprints.id.exceptions.unsafe

import com.simprints.id.exceptions.SimprintsThrowable


open class SimprintsError : Error, SimprintsThrowable {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}
