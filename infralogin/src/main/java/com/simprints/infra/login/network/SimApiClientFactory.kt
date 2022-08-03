package com.simprints.infra.login.network

import com.simprints.infra.network.SimApiClient
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

interface SimApiClientFactory {

    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimApiClient<T>
    fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimApiClient<T>
}
