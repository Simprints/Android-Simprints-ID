package com.simprints.core.network

import kotlin.reflect.KClass
import com.simprints.infra.network.SimApiClient
import com.simprints.infra.network.SimRemoteInterface

// TODO move this into the infralogin
interface SimApiClientFactory {

    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimApiClient<T>
    fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimApiClient<T>
}
