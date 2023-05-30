package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimRemoteInterface
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * This class provides an implementation of FingerprintApiClient factory
 * @see FingerprintApiClientFactory
 */
class FingerprintApiClientFactoryImpl @Inject constructor(
    private val loginManager: LoginManager,
) : FingerprintApiClientFactory {

    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T> =
        FingerprintApiClientImpl(loginManager.buildClient(remoteInterface))
}
