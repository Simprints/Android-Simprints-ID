package com.simprints.infra.login.network

import android.content.Context
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.infra.login.db.FirebaseAuthManager
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.reflect.KClass

internal class SimApiClientFactory @Inject constructor(
    private val simNetwork: SimNetwork,
    @DeviceID private val deviceId: String,
    @ApplicationContext private val ctx: Context,
    @PackageVersionName private val versionName: String,
    private val firebaseAuthManager: FirebaseAuthManager,
) {

    // Not using `inline fun <reified T : SimRemoteInterface>` because it's not possible to
    // create an interface for that or mock it. SimApiClientFactory is injected everywhere, so it's important
    // that we are able to mock it.
    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T> {
        return simNetwork.getSimApiClient(
            remoteInterface,
            ctx,
            simNetwork.getApiBaseUrl(),
            deviceId,
            versionName,
            firebaseAuthManager.getCurrentToken(),
        )
    }

    fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T> {
        return simNetwork.getSimApiClient(
            remoteInterface,
            ctx,
            simNetwork.getApiBaseUrl(),
            deviceId,
            versionName,
            null,
        )
    }
}
