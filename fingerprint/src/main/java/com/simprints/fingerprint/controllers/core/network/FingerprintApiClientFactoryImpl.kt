package com.simprints.fingerprint.controllers.core.network

import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.network.SimRemoteInterface
import kotlin.reflect.KClass

class FingerprintApiClientFactoryImpl(
    private val simApiClientFactory: SimApiClientFactory
) : FingerprintApiClientFactory {

    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T> =
        FingerprintApiClientImpl(simApiClientFactory.buildClient(remoteInterface))
}
