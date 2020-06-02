package com.simprints.fingerprint.controllers.core.network

import com.simprints.id.network.SimApiClient

class FingerprintApiClientImpl<T : FingerprintRemoteInterface>(
    private val simApiClient: SimApiClient<T>
) : FingerprintApiClient<T> {

    override val api: T = simApiClient.api

    override suspend fun <V> executeCall(traceName: String?, networkBlock: suspend (T) -> V): V =
        simApiClient.executeCall(traceName, networkBlock)
}
