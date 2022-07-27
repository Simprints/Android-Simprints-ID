package com.simprints.fingerprint.controllers.core.network

import com.simprints.infra.login.network.SimApiClientFactory
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

class FingerprintApiClientFactoryImpl(
    private val simApiClientFactory: SimApiClientFactory
) : FingerprintApiClientFactory {

    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T> =
        FingerprintApiClientImpl(simApiClientFactory.buildClient(remoteInterface))
}
