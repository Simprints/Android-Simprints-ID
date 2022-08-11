package com.simprints.core.network

import com.simprints.infra.network.SimNetwork.SimApiClient
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

// TODO move this into the infralogin
interface SimApiClientFactory {

    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimApiClient<T>
    fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimApiClient<T>
}
