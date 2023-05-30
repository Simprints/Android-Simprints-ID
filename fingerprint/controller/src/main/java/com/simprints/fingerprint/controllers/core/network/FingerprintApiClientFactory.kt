package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

/**
 * This interface represents a Factory for Fingerprint APIClient.
 */
interface FingerprintApiClientFactory {

    // this function builds the provided remote interface
    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T>
}
