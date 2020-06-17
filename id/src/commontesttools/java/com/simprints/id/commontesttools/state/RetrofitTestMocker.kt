package com.simprints.id.commontesttools.state

import com.simprints.id.network.*
import com.simprints.id.secure.SecureApiInterface
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import io.mockk.every
import io.mockk.mockk

suspend fun replaceSecureApiClientWithFailingClientProvider() = createFailingApiClient<SecureApiInterface>()

suspend inline fun <reified T : SimRemoteInterface> createFailingApiClient(): T {
    val mockBaseUrlProvider: BaseUrlProvider = mockk()
    every { mockBaseUrlProvider.getApiBaseUrl() } returns NetworkConstants.DEFAULT_BASE_URL
    val apiClient = SimApiClientFactoryImpl(mockBaseUrlProvider, "deviceId", mockk(relaxed = true)).buildClient(T::class) as SimApiClientImpl<T>

    return createMockBehaviorService(
        apiClient.retrofit,
        100,
        T::class.java
    ).returningResponse(null)
}
