package com.simprints.infra.login.network

import android.content.Context
import com.simprints.infra.network.SimApiClient
import com.simprints.infra.network.SimApiClientImpl
import com.simprints.infra.network.SimRemoteInterface
import com.simprints.infra.network.url.BaseUrlProvider
import kotlin.reflect.KClass

class SimApiClientFactoryImpl(
    private val baseUrlProvider: BaseUrlProvider,
    private val deviceId: String,
    private val ctx: Context,
    private val versionName: String,
    //private val remoteDbManager: RemoteDbManager,
) : SimApiClientFactory {

    // Not using `inline fun <reified T : SimRemoteInterface>` because it's not possible to
    // create an interface for that or mock it. SimApiClientFactory is injected everywhere, so it's important
    // that we are able to mock it.
    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimApiClient<T> {
        return SimApiClientImpl(
            remoteInterface,
            ctx,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            versionName,
            null,
        )
    }

    override fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimApiClient<T> {
        return SimApiClientImpl(
            remoteInterface,
            ctx,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            versionName,
            null,
        )
    }
}
