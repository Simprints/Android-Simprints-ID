package com.simprints.id.exceptions.safe.session

import com.simprints.id.exceptions.safe.SafeException

class SessionUploadFailureRetryException(cause: Throwable) : SafeException(cause)
