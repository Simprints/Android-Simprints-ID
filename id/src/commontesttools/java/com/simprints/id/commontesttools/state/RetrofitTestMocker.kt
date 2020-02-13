package com.simprints.id.commontesttools.state

import com.simprints.core.network.NetworkConstants
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.secure.SecureApiInterface
import com.simprints.testtools.common.retrofit.createMockBehaviorService


fun replaceSecureApiClientWithFailingClientProvider() = createFailingApiClient<SecureApiInterface>()

inline fun <reified T> createFailingApiClient(): T {
    val apiClient = SimApiClientFactory("deviceId", endpoint = NetworkConstants.baseUrl)
    return createMockBehaviorService(apiClient.build<SecureApiInterface>().retrofit, 100, T::class.java).returningResponse(null)
}
