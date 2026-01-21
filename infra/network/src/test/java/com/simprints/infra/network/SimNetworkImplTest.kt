package com.simprints.infra.network

import com.simprints.infra.network.apiclient.SimApiClientImpl
import com.simprints.infra.network.httpclient.BuildOkHttpClientUseCase
import com.simprints.infra.network.url.BaseUrlProvider
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test

class SimNetworkImplTest {
    @MockK
    private lateinit var baseUrlProvider: BaseUrlProvider

    @MockK
    private lateinit var okHttpClientBuilder: BuildOkHttpClientUseCase
    private val json = Json

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `calling get base url should call the url provider`() {
        val network = SimNetworkImpl(baseUrlProvider, okHttpClientBuilder, json)

        network.getApiBaseUrl()

        verify(exactly = 1) { baseUrlProvider.getApiBaseUrl() }
    }

    @Test
    fun `calling reset base url should call the url provider`() {
        val network = SimNetworkImpl(baseUrlProvider, okHttpClientBuilder, json)

        network.resetApiBaseUrl()

        verify(exactly = 1) { baseUrlProvider.resetApiBaseUrl() }
    }

    @Test
    fun `calling set base url should call the url provider`() {
        val network = SimNetworkImpl(baseUrlProvider, okHttpClientBuilder, json)

        val baseUrl = "FAKE_BASE_URL"

        network.setApiBaseUrl(baseUrl)

        verify(exactly = 1) { baseUrlProvider.setApiBaseUrl(baseUrl) }
    }

    @Test
    fun `calling get api client should return the api client`() {
        val network = SimNetworkImpl(baseUrlProvider, okHttpClientBuilder, json)

        val clientApi = network.getSimApiClient<SimRemoteInterface>(
            mockk(),
            "testDeviceID",
            "testVersion",
            "testAuthToken",
        )

        assert(clientApi is SimApiClientImpl<SimRemoteInterface>)
    }

    @Test
    fun `calling get api client with no token should return the api client`() {
        val network = SimNetworkImpl(baseUrlProvider, okHttpClientBuilder, json)

        val clientApi = network.getSimApiClient<SimRemoteInterface>(
            mockk(),
            "testDeviceID",
            "testVersion",
            null,
        )

        assert(clientApi is SimApiClientImpl<SimRemoteInterface>)
    }
}
