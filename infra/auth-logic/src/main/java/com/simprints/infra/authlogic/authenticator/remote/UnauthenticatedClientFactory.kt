package com.simprints.infra.authlogic.authenticator.remote

import android.content.Context
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.reflect.KClass

internal class UnauthenticatedClientFactory @Inject constructor(
    private val simNetwork: SimNetwork,
    @DeviceID private val deviceId: String,
    @ApplicationContext private val ctx: Context,
    @PackageVersionName private val versionName: String,
) {

    fun <T : SimRemoteInterface> build(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T> =
        simNetwork.getSimApiClient(
            remoteInterface,
            ctx,
            simNetwork.getApiBaseUrl(),
            deviceId,
            versionName,
            null,
        )
}
