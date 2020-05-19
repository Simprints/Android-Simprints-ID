package com.simprints.id.commontesttools.state

import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.NetworkConstants
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.secure.SecureApiInterface
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import io.mockk.every
import io.mockk.mockk

fun replaceSecureApiClientWithFailingClientProvider() = createFailingApiClient<SecureApiInterface>()

inline fun <reified T> createFailingApiClient(): T {
    val mockBaseUrlProvider: BaseUrlProvider = mockk()
    every { mockBaseUrlProvider.getApiBaseUrl() } returns NetworkConstants.DEFAULT_BASE_URL
    val apiClient = SimApiClientFactory(mockBaseUrlProvider, "deviceId")

    return createMockBehaviorService(
        apiClient.build<SecureApiInterface>().retrofit,
        100,
        T::class.java
    ).returningResponse(null)
}
