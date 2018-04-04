package com.simprints.id.exceptions.unsafe


class UnexpectedSyncError(cause: Throwable, message: String = "UnexpectedSyncError")
    : SimprintsError(message, cause)
