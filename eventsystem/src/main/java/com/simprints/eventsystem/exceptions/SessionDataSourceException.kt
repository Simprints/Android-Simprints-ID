package com.simprints.eventsystem.exceptions

open class SessionDataSourceException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
