package com.simprints.infra.login.network

import android.content.Context
import com.simprints.infra.login.db.RemoteDbManager
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import javax.inject.Inject
import kotlin.reflect.KClass

internal class SimApiClientFactoryImpl @Inject constructor(
    private val baseUrlProvider: SimNetwork,
    private val deviceId: String,
    private val ctx: Context,
    private val versionName: String,
    private val remoteDbManager: RemoteDbManager,
) : SimApiClientFactory {

    // Not using `inline fun <reified T : SimRemoteInterface>` because it's not possible to
    // create an interface for that or mock it. SimApiClientFactory is injected everywhere, so it's important
    // that we are able to mock it.
    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T> {
        return baseUrlProvider.getSimApiClient(
            remoteInterface,
            ctx,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            versionName,
            remoteDbManager.getCurrentToken(),
        )
    }

    override fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T> {
        return baseUrlProvider.getSimApiClient(
            remoteInterface,
            ctx,
            baseUrlProvider.getApiBaseUrl(),
            deviceId,
            versionName,
            null,
        )
    }
}
