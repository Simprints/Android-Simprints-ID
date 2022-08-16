package com.simprints.infra.login.network

import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

internal interface SimApiClientFactory {

    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T>
    fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T>
}
