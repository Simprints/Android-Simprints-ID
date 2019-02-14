package com.simprints.id.exceptions.safe.sync

import com.simprints.id.exceptions.safe.SafeException

class TransientSyncFailureException(
    message: String = "TransientSyncFailureException",
    cause: Throwable? = null
)
    : SafeException(message, cause)
