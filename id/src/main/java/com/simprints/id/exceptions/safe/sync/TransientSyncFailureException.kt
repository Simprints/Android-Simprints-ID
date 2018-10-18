package com.simprints.id.exceptions.safe.sync

import com.simprints.id.exceptions.safe.SimprintsException

class TransientSyncFailureException(
    message: String = "TransientSyncFailureException",
    cause: Throwable? = null
)
    : SimprintsException(message, cause)
