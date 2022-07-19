package com.simprints.infra.network

import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException

interface SimApiClient<T : SimRemoteInterface> {

    val api: T

    /**
     * Execute the network call
     * @throws BackendMaintenanceException if the backend is in maintenance mode
     * @throws SyncCloudIntegrationException if the backend returns an error code
     */
    suspend fun <V> executeCall(networkBlock: suspend (T) -> V): V
}
