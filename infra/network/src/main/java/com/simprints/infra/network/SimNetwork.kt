package com.simprints.infra.network

import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import kotlin.reflect.KClass

interface SimNetwork {
    interface SimApiClient<T : SimRemoteInterface> {
        val api: T

        /**
         * Execute the network call
         * @throws BackendMaintenanceException if the backend is in maintenance mode
         * @throws SyncCloudIntegrationException if the backend returns an error code
         */
        suspend fun <V> executeCall(networkBlock: suspend (T) -> V): V
    }

    fun <T : SimRemoteInterface> getSimApiClient(
        remoteInterface: KClass<T>,
        deviceId: String,
        versionName: String,
        authToken: String?,
    ): SimApiClient<T>

    fun getApiBaseUrl(): String

    fun getApiBaseUrlPrefix(): String

    fun setApiBaseUrl(apiBaseUrl: String?)

    fun resetApiBaseUrl()
}
