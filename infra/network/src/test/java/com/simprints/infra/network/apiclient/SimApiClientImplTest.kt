package com.simprints.infra.network.apiclient

import com.google.common.truth.Truth.*
import com.simprints.infra.network.Fake
import com.simprints.infra.network.FakeRetrofitInterface
import com.simprints.infra.network.exceptions.ApiError
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.infra.network.httpclient.BuildOkHttpClientUseCase
import com.simprints.infra.serialization.SimJson
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
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
    lateinit var persistentLogger: com.simprints.logging.persistent.PersistentLogger

    private val backendMaintenanceErrorBody = SimJson.encodeToString(ApiError("002"))
    private val nonMaintenanceErrorBody = SimJson.encodeToString(ApiError("999"))
    private val successBody = SimJson.encodeToString(Fake("value"))

    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClientBuilder: BuildOkHttpClientUseCase

    /** Client with attempts=1: no retries, single attempt only. */
    private lateinit var noRetryClient: SimApiClientImpl<FakeRetrofitInterface>

    /** Client with attempts=3: retries up to 3 times for retryable errors. */
    private lateinit var retryClient: SimApiClientImpl<FakeRetrofitInterface>

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockWebServer = MockWebServer()
        mockWebServer.start()

        httpClientBuilder = BuildOkHttpClientUseCase(
            mockk(relaxed = true),
            networkCache,
            persistentLogger,
        )

        noRetryClient = SimApiClientImpl(
            FakeRetrofitInterface::class,
            httpClientBuilder,
            mockWebServer.url("/").toString(),
            deviceId = "device-1",
            versionName = "1.0",
            attempts = 1,
        )

        retryClient = SimApiClientImpl(
            FakeRetrofitInterface::class,
            httpClientBuilder,
            mockWebServer.url("/").toString(),
            deviceId = "device-1",
            versionName = "1.0",
            attempts = 3,
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // Happy path

    @Test
    fun `returns deserialized domain object on 200 response`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(successBody))

        val result = noRetryClient.executeCall { it.get() }

        assertThat(result).isEqualTo(Fake("value"))
        assertThat(mockWebServer.requestCount).isEqualTo(1)
    }

    @Test
    fun `returns successful Response wrapper on 200 response`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val result = noRetryClient.executeCall { it.getResponse() }

        assertThat(result.isSuccessful).isTrue()
        assertThat(result.code()).isEqualTo(200)
        assertThat(result.body()?.string()).isEqualTo("ok")
    }

    // 4xx errors – no retry, wrapped in SyncCloudIntegrationException

    @Test
    fun `throws SyncCloudIntegrationException on 401 `() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val exception = assertThrows<SyncCloudIntegrationException> {
            noRetryClient.executeCall { it.get() }
        }
        assertThat(exception.cause).isInstanceOf(HttpException::class.java)
        assertThat(exception.httpStatusCode()).isEqualTo(401)
    }

    @Test
    fun `does not retry on 4xx error`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        assertThrows<SyncCloudIntegrationException> {
            retryClient.executeCall { it.get() }
        }
        // Only 1 request should have been made despite retry client
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS)).isNotNull()
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS)).isNull()
    }

    @Test
    fun `Response wrapper with non-2xx code throws SyncCloudIntegrationException`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val exception = assertThrows<SyncCloudIntegrationException> {
            val r = noRetryClient.executeCall { it.getResponse() }
        }
        assertThat(exception.cause).isInstanceOf(HttpException::class.java)
        assertThat(exception.httpStatusCode()).isEqualTo(401)
    }

    // 5xx retryable errors (500, 502, 503 non-maintenance)

    @Test
    fun `retries on 500 up to the configured attempts`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(500)) }

        assertThrows<SyncCloudIntegrationException> {
            retryClient.executeCall { it.get() }
        }
        assertThat(mockWebServer.requestCount).isEqualTo(3)
    }

    @Test
    fun `retries on 502 up to the configured attempts`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(502)) }

        assertThrows<SyncCloudIntegrationException> {
            retryClient.executeCall { it.get() }
        }
        assertThat(mockWebServer.requestCount).isEqualTo(3)
    }

    @Test
    fun `retries on 503 non-maintenance error up to the configured attempts`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody(nonMaintenanceErrorBody)) }

        assertThrows<SyncCloudIntegrationException> {
            retryClient.executeCall { it.get() }
        }
        assertThat(mockWebServer.requestCount).isEqualTo(3)
    }

    @Test
    fun `succeeds after retryable 500 error on subsequent attempt`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setBody(successBody))

        val result = retryClient.executeCall { it.get() }

        assertThat(result).isEqualTo(Fake("value"))
        assertThat(mockWebServer.requestCount).isEqualTo(2)
    }

    @Test
    fun `final exception after exhausting retries on 5xx is SyncCloudIntegrationException`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(500)) }

        val exception = assertThrows<SyncCloudIntegrationException> {
            retryClient.executeCall { it.get() }
        }
        assertThat(exception.cause).isInstanceOf(HttpException::class.java)
    }

    @Test
    fun `retries on 500 with Response wrapper`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(500)) }

        assertThrows<SyncCloudIntegrationException> {
            val r = retryClient.executeCall { it.getResponse() }
        }
        assertThat(mockWebServer.requestCount).isEqualTo(3)
    }

    @Test
    fun `retries on 502 with Response wrapper`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(502)) }

        assertThrows<SyncCloudIntegrationException> {
            val r = retryClient.executeCall { it.getResponse() }
        }
        assertThat(mockWebServer.requestCount).isEqualTo(3)
    }

    @Test
    fun `retries on 503 non-maintenance error with Response wrapper`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody(nonMaintenanceErrorBody)) }

        assertThrows<SyncCloudIntegrationException> {
            val r = retryClient.executeCall { it.getResponse() }
        }
        assertThat(mockWebServer.requestCount).isEqualTo(3)
    }

    // Backend maintenance (503 with error code "002")

    @Test
    fun `throws BackendMaintenanceException on 503 with maintenance error body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody(backendMaintenanceErrorBody))

        assertThrows<BackendMaintenanceException> {
            noRetryClient.executeCall { it.get() }
        }
    }

    @Test
    fun `throws BackendMaintenanceException on 503 with response wrapper`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody(backendMaintenanceErrorBody))

        assertThrows<BackendMaintenanceException> {
            val r = noRetryClient.executeCall { it.getResponse() }
        }
    }

    @Test
    fun `does not retry on backend maintenance error`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody(backendMaintenanceErrorBody)) }

        assertThrows<BackendMaintenanceException> {
            retryClient.executeCall { it.get() }
        }
        // Maintenance is not retryable – only 1 request
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS)).isNotNull()
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS)).isNull()
    }

    @Test
    fun `BackendMaintenanceException has null estimatedOutage when retry-after header is absent`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody(backendMaintenanceErrorBody))

        val exception = assertThrows<BackendMaintenanceException> {
            noRetryClient.executeCall { it.get() }
        }
        assertThat(exception.estimatedOutage).isNull()
    }

    @Test
    fun `BackendMaintenanceException carries estimatedOutage from retry-after header`() = runTest {
        val estimatedOutage = 300L
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(503)
                .setBody(backendMaintenanceErrorBody)
                .addHeader("retry-after", estimatedOutage.toString()),
        )

        val exception = assertThrows<BackendMaintenanceException> {
            noRetryClient.executeCall { it.get() }
        }
        assertThat(exception.estimatedOutage).isEqualTo(estimatedOutage)
    }

    @Test
    fun `BackendMaintenanceException has null estimatedOutage when retry-after header is non-numeric`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(503)
                .setBody(backendMaintenanceErrorBody)
                .addHeader("retry-after", "not-a-number"),
        )

        val exception = assertThrows<BackendMaintenanceException> {
            noRetryClient.executeCall { it.get() }
        }
        assertThat(exception.estimatedOutage).isNull()
    }

    @Test
    fun `503 without maintenance error body is treated as retryable and eventually throws SyncCloudIntegrationException`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody("{}")) }

        assertThrows<SyncCloudIntegrationException> {
            retryClient.executeCall { it.get() }
        }
    }

    // -------------------------------------------------------------------------
    // Network connection errors
    // -------------------------------------------------------------------------

    @Test
    fun `throws NetworkConnectionException when server closes the connection immediately`() = runTest {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertThrows<NetworkConnectionException> {
            noRetryClient.executeCall { it.get() }
        }
    }

    @Test
    fun `does not retry on network connection error`() = runTest {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        mockWebServer.enqueue(MockResponse().setBody(successBody))

        assertThrows<NetworkConnectionException> {
            retryClient.executeCall { it.get() }
        }
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS)).isNotNull()
        assertThat(mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS)).isNull()
    }

    @Test
    fun `NetworkConnectionException cause is an IOException`() = runTest {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val exception = assertThrows<NetworkConnectionException> {
            noRetryClient.executeCall { it.get() }
        }
        assertThat(exception.cause).isInstanceOf(java.io.IOException::class.java)
    }
}
