package com.simprints.libdata.exceptions.unsafe


class UnexpectedSyncError(cause: Throwable, message: String = "UnexpectedSyncError")
    : Error(message, cause)
