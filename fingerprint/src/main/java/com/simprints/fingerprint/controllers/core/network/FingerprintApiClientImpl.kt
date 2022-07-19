package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.network.SimApiClient
import com.simprints.infra.network.SimRemoteInterface

class FingerprintApiClientImpl<T : SimRemoteInterface>(
    private val simApiClient: SimApiClient<T>
) : FingerprintApiClient<T> {

    override val api: T = simApiClient.api

    override suspend fun <V> executeCall(networkBlock: suspend (T) -> V): V =
        simApiClient.executeCall(networkBlock)
}
