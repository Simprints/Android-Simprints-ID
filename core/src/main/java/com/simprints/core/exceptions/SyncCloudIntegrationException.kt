package com.simprints.core.exceptions

class SyncCloudIntegrationException(
    message: String = "SyncCloudIntegrationException",
    cause: Throwable
): UnexpectedException(message, cause)
