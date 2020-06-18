package com.simprints.fingerprint.controllers.core.network

import com.simprints.id.network.SimApiClientFactory
import kotlin.reflect.KClass

class FingerprintApiClientFactoryImpl(
    private val simApiClientFactory: SimApiClientFactory
) : FingerprintApiClientFactory {

    override suspend fun <T : FingerprintRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T> =
        FingerprintApiClientImpl(simApiClientFactory.buildClient(remoteInterface))
}
