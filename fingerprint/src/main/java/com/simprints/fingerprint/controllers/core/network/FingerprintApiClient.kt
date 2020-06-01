package com.simprints.fingerprint.controllers.core.network

import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.network.SimRemoteInterface

interface FingerprintApiClient<T : SimRemoteInterface> {

    val api: T

    /** @throws SyncCloudIntegrationException */
    suspend fun <V> executeCall(traceName: String?, networkBlock: suspend (T) -> V): V
}
