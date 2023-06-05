package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.network.SimRemoteInterface
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * This class provides an implementation of FingerprintApiClient factory
 * @see FingerprintApiClientFactory
 */
class FingerprintApiClientFactoryImpl @Inject constructor(
    private val authStore: AuthStore,
) : FingerprintApiClientFactory {

    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T> =
        FingerprintApiClientImpl(authStore.buildClient(remoteInterface))
}
