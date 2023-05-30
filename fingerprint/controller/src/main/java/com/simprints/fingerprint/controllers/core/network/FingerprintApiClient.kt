package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.network.SimRemoteInterface

/**
 * This interface represents an API network interface for fingerprint requests
 */
interface FingerprintApiClient<T : SimRemoteInterface> {

    // the remote interface that executes the network requests
    val api: T

    suspend fun <V> executeCall(networkBlock: suspend (T) -> V): V
}
