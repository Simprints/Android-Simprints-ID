package com.simprints.id.exceptions.safe

import com.simprints.core.exceptions.SafeException

class FailedToRetrieveUserLocation(cause: Throwable) : SafeException(cause)
