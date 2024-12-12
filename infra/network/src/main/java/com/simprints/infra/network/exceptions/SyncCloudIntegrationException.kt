package com.simprints.infra.network.exceptions

import retrofit2.HttpException

class SyncCloudIntegrationException(
    message: String = "SyncCloudIntegrationException",
    cause: Throwable,
) : RuntimeException(message, cause) {
    fun httpStatusCode(): Int? = (cause as? HttpException)?.code()
}
