package com.simprints.infra.network

import android.content.Context
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

        companion object {
            fun <T : SimRemoteInterface> getSimApiClient(
                remoteInterface: KClass<T>,
                ctx: Context,
                url: String,
                deviceId: String,
                versionName: String,
                authToken: String?
            ): SimApiClient<T> {
                return SimNetworkClientImpl.getSimApiClient(
                    remoteInterface,
                    ctx,
                    url,
                    deviceId,
                    versionName,
                    authToken
                )
            }

        }

    }

    fun getApiBaseUrl(): String
    fun setApiBaseUrl(apiBaseUrl: String?)
    fun resetApiBaseUrl()

}
