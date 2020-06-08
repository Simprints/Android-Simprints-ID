package com.simprints.id.network

import com.google.gson.Gson
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.common.RemoteDbManager
import kotlin.reflect.KClass

class SimApiClientFactoryImpl(
    val baseUrlProvider: BaseUrlProvider,
    val deviceId: String,
    private val remoteDbManager: RemoteDbManager,
    private val jsonAdapter: Gson = JsonHelper.gson
): SimApiClientFactory {

    // Not using `inline fun <reified T : SimRemoteInterface>` because it's not possible to
    // create an interface for that or mock it. SimApiClientFactory is injected everywhere, so it's important
    // that we are able to mock it.
    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimApiClient<T> {
        return SimApiClientImpl(
            remoteInterface,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            remoteDbManager.getCurrentToken(),
            jsonAdapter
        )
    }

    override fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimApiClient<T> {
        return SimApiClientImpl(
            remoteInterface,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            null,
            jsonAdapter
        )
    }
}
