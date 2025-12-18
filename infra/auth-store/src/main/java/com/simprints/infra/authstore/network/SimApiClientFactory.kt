package com.simprints.infra.authstore.network

import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.infra.authstore.db.FirebaseAuthManager
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import javax.inject.Inject
import kotlin.reflect.KClass

internal class SimApiClientFactory @Inject constructor(
    private val simNetwork: SimNetwork,
    @param:DeviceID private val deviceId: String,
    @param:PackageVersionName private val versionName: String,
    private val firebaseAuthManager: FirebaseAuthManager,
) {
    // Not using `inline fun <reified T : SimRemoteInterface>` because it's not possible to
    // create an interface for that or mock it. SimApiClientFactory is injected everywhere, so it's important
    // that we are able to mock it.
    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T> = simNetwork.getSimApiClient(
        remoteInterface,
        deviceId,
        versionName,
        firebaseAuthManager.getCurrentToken(),
    )
}
