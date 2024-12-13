package com.simprints.infra.network.exceptions

class RetryableCloudException(
    message: String = "RetryableCloudException",
    cause: Throwable,
) : RuntimeException(message, cause)
