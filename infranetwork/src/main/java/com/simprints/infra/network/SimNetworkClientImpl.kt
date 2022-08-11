package com.simprints.infra.network

import android.content.Context
import com.simprints.infra.network.apiclient.SimApiClient
import com.simprints.infra.network.apiclient.SimApiClientImpl
import kotlin.reflect.KClass

internal class SimNetworkClientImpl<T : SimRemoteInterface>(private val simApiClient: SimApiClient<T>) :
    SimNetwork.SimApiClient<T> {

    override val api: T
        get() = simApiClient.api

    override suspend fun <V> executeCall(networkBlock: suspend (T) -> V): V {
        return simApiClient.executeCall(networkBlock)
    }

    companion object {
        fun <T : SimRemoteInterface> getSimApiClient(
            remoteInterface: KClass<T>,
            ctx: Context,
            url: String,
            deviceId: String,
            versionName: String,
            authToken: String?
        ): SimNetwork.SimApiClient<T> {
            return SimNetworkClientImpl(
                SimApiClientImpl(
                    remoteInterface,
                    ctx,
                    url,
                    deviceId,
                    versionName,
                    authToken
                )
            )
        }

    }

}
