package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

interface FingerprintApiClientFactory {

    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T>
}
