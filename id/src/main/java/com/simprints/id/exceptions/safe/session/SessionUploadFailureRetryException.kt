package com.simprints.id.exceptions.safe.session

import com.simprints.id.exceptions.safe.SimprintsException


open class SessionUploadFailureRetryException : SimprintsException {
    constructor(): super("SessionUploadFailureRetryException")
    constructor(cause: Throwable): super(cause)
}
