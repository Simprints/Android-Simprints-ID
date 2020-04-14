package com.simprints.id.exceptions.safe.session

open class SessionDataSourceException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
