package com.simprints.fingerprint.controllers.core.network

import kotlin.reflect.KClass

interface FingerprintApiClientFactory {

    suspend fun <T : FingerprintRemoteInterface> buildClient(remoteInterface: KClass<T>): FingerprintApiClient<T>
}
