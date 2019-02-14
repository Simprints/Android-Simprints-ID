package com.simprints.id.exceptions.safe.data.db

import com.simprints.id.exceptions.safe.SafeException


class LocalDbRecoveryFailedException(message: String = "LocalDbRecoveryFailedException", cause: Throwable) // STOPSHIP remove
    : SafeException(message, cause)
