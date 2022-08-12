package com.simprints.infra.network

import com.simprints.infra.network.apiclient.SimApiClientImpl
import com.simprints.infra.network.url.BaseUrlProvider
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class SimNetworkImplTest {

    @Test
    fun `calling get base url should call the url provider`() {
        val baseUrlProvider: BaseUrlProvider = spyk()
        val network = SimNetworkImpl(baseUrlProvider)

        network.getApiBaseUrl()

        verify(exactly = 1) { baseUrlProvider.getApiBaseUrl() }
    }

    @Test
    fun `calling reset base url should call the url provider`() {
        val baseUrlProvider: BaseUrlProvider = spyk()
        val network = SimNetworkImpl(baseUrlProvider)

        network.resetApiBaseUrl()

        verify(exactly = 1) { baseUrlProvider.resetApiBaseUrl() }
    }

    @Test
    fun `calling set base url should call the url provider`() {
        val baseUrlProvider: BaseUrlProvider = spyk()
        val network = SimNetworkImpl(baseUrlProvider)

        val baseUrl = "FAKE_BASE_URL"

        network.setApiBaseUrl(baseUrl)

        verify(exactly = 1) { baseUrlProvider.setApiBaseUrl(baseUrl) }
    }

    @Test
    fun `calling get api client should return the api client`() {
        val baseUrlProvider: BaseUrlProvider = spyk()
        val network = SimNetworkImpl(baseUrlProvider)

        val clientApi = network.getSimApiClient<SimRemoteInterface>(
            mockk(),
            mockk(),
            "testUrl",
            "testDeviceID",
            "testVersion",
            "testAuthToken"
        )

        assert(clientApi is SimApiClientImpl<SimRemoteInterface>)
    }

    @Test
    fun `calling get api client with no token should return the api client`() {
        val baseUrlProvider: BaseUrlProvider = spyk()
        val network = SimNetworkImpl(baseUrlProvider)

        val clientApi = network.getSimApiClient<SimRemoteInterface>(
            mockk(),
            mockk(),
            "testUrl",
            "testDeviceID",
            "testVersion",
            null
        )
        
        assert(clientApi is SimApiClientImpl<SimRemoteInterface>)
    }

}
