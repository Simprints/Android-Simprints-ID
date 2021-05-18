package com.simprints.id.exceptions.unexpected

import com.simprints.core.exceptions.UnexpectedException

class SyncCloudIntegrationException(
    message: String = "SyncCloudIntegrationException",
    cause: Throwable
): UnexpectedException(message, cause)
