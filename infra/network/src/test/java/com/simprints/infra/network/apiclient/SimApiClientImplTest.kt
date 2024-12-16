package com.simprints.infra.network.apiclient

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.network.FakeRetrofitInterface
import com.simprints.infra.network.exceptions.ApiError
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.infra.network.httpclient.DefaultOkHttpClientBuilder
import com.simprints.logging.persistent.PersistentLogger
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.IOException
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class SimApiClientImplTest {
    @MockK
    lateinit var networkCache: okhttp3.Cache

    @MockK
    lateinit var persistentLogger: PersistentLogger

    private val backendMaintenanceErrorBody =
        jacksonObjectMapper().writeValueAsString(ApiError("002"))
    private lateinit var mockWebServer: MockWebServer
    private lateinit var simApiClientImpl: SimApiClientImpl<FakeRetrofitInterface>

    private lateinit var httpClientBuilder: DefaultOkHttpClientBuilder

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockWebServer = MockWebServer()
        mockWebServer.start()
        httpClientBuilder = DefaultOkHttpClientBuilder(
            mockk(),
            networkCache,
            persistentLogger,
        )

        simApiClientImpl = SimApiClientImpl(
            FakeRetrofitInterface::class,
            httpClientBuilder,
            mockWebServer.url("/").toString(),
            "",
            "",
            attempts = 2,
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should throw a backend maintenance exception without estimated outage when it's a maintenance error without header and not retry`() =
        runTest {
            val response = MockResponse()
            response.setResponseCode(503)
            response.setBody(backendMaintenanceErrorBody)
            mockWebServer.enqueue(response)
            mockWebServer.enqueue(response)

            val exception = assertThrows<BackendMaintenanceException> {
                simApiClientImpl.executeCall { it.get() }
            }
            assertThat(exception.estimatedOutage).isNull()
        }

    @Test
    fun `should throw a backend maintenance exception with estimated outage when it's a maintenance error with header and not retry`() =
        runTest {
            val estimatedOutage = 4224
            val response = MockResponse()
            response.setResponseCode(503)
            response.setBody(jacksonObjectMapper().writeValueAsString(ApiError("002")))
            response.setHeader("Retry-After", estimatedOutage)
            mockWebServer.enqueue(response)
            mockWebServer.enqueue(response)

            val exception = assertThrows<BackendMaintenanceException> {
                simApiClientImpl.executeCall { it.get() }
            }
            assertThat(exception.estimatedOutage).isEqualTo(estimatedOutage)
            assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
            assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNull()
        }

    @Test
    fun `should throw a sync cloud integration exception when the response code is 500 and retry`() = runTest {
        val response = MockResponse()
        response.setResponseCode(500)
        mockWebServer.enqueue(response)
        mockWebServer.enqueue(response)

        val exception = assertThrows<SyncCloudIntegrationException> {
            simApiClientImpl.executeCall { it.get() }
        }
        assertThat(exception.cause).isInstanceOf(HttpException::class.java)
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
    }

    @Test
    fun `should throw a sync cloud integration exception when the response code is 502 and retry`() = runTest {
        val response = MockResponse()
        response.setResponseCode(502)
        mockWebServer.enqueue(response)
        mockWebServer.enqueue(response)

        val exception = assertThrows<SyncCloudIntegrationException> {
            simApiClientImpl.executeCall { it.get() }
        }
        assertThat(exception.cause).isInstanceOf(HttpException::class.java)
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
    }

    @Test
    fun `should throw a sync cloud integration exception when the response code is 503 and retry`() = runTest {
        val response = MockResponse()
        response.setResponseCode(503)
        response.setBody(jacksonObjectMapper().writeValueAsString(ApiError("001")))
        mockWebServer.enqueue(response)
        mockWebServer.enqueue(response)

        val exception = assertThrows<SyncCloudIntegrationException> {
            simApiClientImpl.executeCall { it.get() }
        }
        assertThat(exception.cause).isInstanceOf(HttpException::class.java)
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
    }

    @Test
    fun `should throw a sync cloud integration exception when it's 4xx error and not retry`() = runTest {
        val response = MockResponse()
        response.setResponseCode(400)
        mockWebServer.enqueue(response)
        mockWebServer.enqueue(response)

        val exception = assertThrows<SyncCloudIntegrationException> {
            simApiClientImpl.executeCall { it.get() }
        }
        assertThat(exception.cause).isInstanceOf(HttpException::class.java)
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNull()
    }

    @Test
    fun `should throw a network connection exception when no response is received and not retry`() = runTest {
        val failedResponse = MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        mockWebServer.enqueue(failedResponse)
        val successfulResponse = MockResponse()
        mockWebServer.enqueue(successfulResponse)

        val exception = assertThrows<NetworkConnectionException> {
            simApiClientImpl.executeCall { it.get() }
        }
        assertThat(exception.cause).isInstanceOf(IOException::class.java)
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNotNull()
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MICROSECONDS)).isNull()
    }
}
