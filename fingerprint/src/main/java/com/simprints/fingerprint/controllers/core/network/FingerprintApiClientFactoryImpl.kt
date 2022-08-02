package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

class FingerprintApiClientFactoryImpl(
    private val loginManager: LoginManager,
) : FingerprintApiClientFactory {

    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T> =
        FingerprintApiClientImpl(loginManager.buildClient(remoteInterface))
}
