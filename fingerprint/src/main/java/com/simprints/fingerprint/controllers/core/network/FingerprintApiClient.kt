package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.network.SimRemoteInterface

interface FingerprintApiClient<T : SimRemoteInterface> {

    val api: T

    suspend fun <V> executeCall(networkBlock: suspend (T) -> V): V
}
