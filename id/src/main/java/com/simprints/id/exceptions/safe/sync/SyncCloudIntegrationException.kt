package com.simprints.id.exceptions.safe.sync

import com.simprints.id.exceptions.safe.SafeException

class SyncCloudIntegrationException(
    message: String = "SyncCloudIntegrationException",
    cause: Throwable
): SafeException(message, cause)
