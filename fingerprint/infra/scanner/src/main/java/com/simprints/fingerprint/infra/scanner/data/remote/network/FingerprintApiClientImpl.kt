package com.simprints.fingerprint.infra.scanner.data.remote.network

import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface

/**
 * This class provides an implementation of FingerprintApiClient
 * @see FingerprintApiClient
 */
internal class FingerprintApiClientImpl<T : SimRemoteInterface>(
    private val simApiClient: SimNetwork.SimApiClient<T>,
) : FingerprintApiClient<T> {
    override val api: T = simApiClient.api

    override suspend fun <V> executeCall(networkBlock: suspend (T) -> V): V = simApiClient.executeCall(networkBlock)
}
