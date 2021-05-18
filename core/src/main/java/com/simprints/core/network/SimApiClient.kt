package com.simprints.core.network

import com.simprints.core.exceptions.SyncCloudIntegrationException

interface SimApiClient<T : SimRemoteInterface> {

    val api: T

    // SyncCloudIntegrationException if an integration issue occurs or Throwable otherwise
    @Throws(SyncCloudIntegrationException::class)
    suspend fun <V> executeCall(traceName: String?,
                                networkBlock: suspend (T) -> V): V

}
