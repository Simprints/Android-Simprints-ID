package com.simprints.infra.network.exceptions

class SyncCloudIntegrationException(
    message: String = "SyncCloudIntegrationException",
    cause: Throwable,
) :
    RuntimeException(message, cause)
