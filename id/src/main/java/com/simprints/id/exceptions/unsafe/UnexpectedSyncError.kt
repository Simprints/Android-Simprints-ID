package com.simprints.id.exceptions.unsafe


class UnexpectedSyncError(cause: Throwable, message: String = "UnexpectedSyncError")
    : Error(message, cause)
