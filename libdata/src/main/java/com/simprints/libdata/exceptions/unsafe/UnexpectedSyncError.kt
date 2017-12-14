package com.simprints.libdata.exceptions.unsafe


class UnexpectedSyncError(message: String = "UnexpectedSyncError", cause: Throwable)
    : Error(message, cause)
