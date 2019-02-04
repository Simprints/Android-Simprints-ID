package com.simprints.id.exceptions.safe.session

import com.simprints.id.exceptions.safe.SimprintsException


open class SessionUploadFailureRetryException(cause: Throwable) : SimprintsException(cause)
