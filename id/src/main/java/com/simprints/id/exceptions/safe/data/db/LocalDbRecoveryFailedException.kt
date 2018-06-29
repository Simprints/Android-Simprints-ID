package com.simprints.id.exceptions.safe.data.db

import com.simprints.id.exceptions.safe.SimprintsException


class LocalDbRecoveryFailedException(message: String = "LocalDbRecoveryFailedException", cause: Throwable)
    : SimprintsException(message, cause)
