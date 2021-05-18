package com.simprints.id.network

import com.simprints.core.network.SimRemoteInterface
import kotlin.reflect.KClass

interface SimApiClientFactory {

    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimApiClient<T>
    fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimApiClient<T>
}
