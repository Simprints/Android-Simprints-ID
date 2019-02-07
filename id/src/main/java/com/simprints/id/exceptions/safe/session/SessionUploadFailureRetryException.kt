package com.simprints.id.exceptions.safe.session

import com.simprints.id.exceptions.safe.SimprintsException

class SessionUploadFailureRetryException(cause: Throwable) : SimprintsException(cause)
