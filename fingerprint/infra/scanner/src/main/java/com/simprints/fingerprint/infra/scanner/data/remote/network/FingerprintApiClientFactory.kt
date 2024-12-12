package com.simprints.fingerprint.infra.scanner.data.remote.network

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.network.SimRemoteInterface
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * This class provides an implementation of FingerprintApiClient factory
 * @see FingerprintApiClientFactory
 */
internal class FingerprintApiClientFactory @Inject constructor(
    private val authStore: AuthStore,
) {
    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T> =
        FingerprintApiClientImpl(authStore.buildClient(remoteInterface))
}
