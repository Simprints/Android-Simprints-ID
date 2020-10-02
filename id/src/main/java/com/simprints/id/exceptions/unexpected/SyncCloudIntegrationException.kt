package com.simprints.id.exceptions.unexpected

import com.simprints.id.exceptions.UnexpectedException

class SyncCloudIntegrationException(
    message: String = "SyncCloudIntegrationException",
    cause: Throwable
): UnexpectedException(message, cause)
