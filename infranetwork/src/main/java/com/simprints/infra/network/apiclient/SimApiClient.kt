package com.simprints.infra.network.apiclient

import android.content.Context
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

internal interface SimApiClient<T : SimRemoteInterface> {

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
            return SimApiClientImpl(
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
