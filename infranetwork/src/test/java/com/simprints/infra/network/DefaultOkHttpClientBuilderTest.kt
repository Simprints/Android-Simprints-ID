package com.simprints.infra.network

import android.content.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.RetryableCloudException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class DefaultOkHttpClientBuilderTest {

    // mock server to read http request parameters
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @RelaxedMockK
    lateinit var ctx: Context

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should include SID version in request headers`() {
        mockWebServer.enqueue(MockResponse())

        val versionName = "cxxlvxzn.12.2049"

        // create okHttp client using default builder
        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, "", "", versionName)
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.getHeader(DefaultOkHttpClientBuilder.USER_AGENT_HEADER))
            .isEqualTo("SimprintsID/$versionName")
    }

    @Test
    fun `should include provided device ID in request headers`() {
        mockWebServer.enqueue(MockResponse())

        val deviceId = "symeAwxomedyvexeid"

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, "", deviceId, "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.getHeader(DefaultOkHttpClientBuilder.DEVICE_ID_HEADER))
            .isEqualTo(deviceId)
    }

    @Test
    fun `should not include auth token in request headers, when auth token is null`() {
        mockWebServer.enqueue(MockResponse())

        val okHttpBuilder = DefaultOkHttpClientBuilder()

        okHttpClient = okHttpBuilder
            .get(ctx, null, "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()


        assertThat(recordedRequest.getHeader(DefaultOkHttpClientBuilder.AUTHORIZATION_HEADER)).isNull()
    }

    @Test
    fun `should include provided auth token in request headers`() {
        mockWebServer.enqueue(MockResponse())

        val authToken = "eyxSomeAwesomeAuth.TokenThatIsUsed.ForUnitTesting"

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, authToken, "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()


        assertThat(recordedRequest.getHeader(DefaultOkHttpClientBuilder.AUTHORIZATION_HEADER))
            .isEqualTo("Bearer $authToken")
    }

    @Test
    fun `should throw a backend maintenance exception without estimated outage when it's a maintenance error without header`() {
        val response = MockResponse()
        response.setResponseCode(503)
        response.setBody(jacksonObjectMapper().writeValueAsString(ApiError("002")))
        mockWebServer.enqueue(response)

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, "", "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        val exception = assertThrows(BackendMaintenanceException::class.java) {
            okHttpClient.newCall(mockHttpRequest).execute()
        }
        assertThat(exception.estimatedOutage).isNull()
    }

    @Test
    fun `should throw a backend maintenance exception with estimated outage when it's a maintenance error with header`() {
        val estimatedOutage = 4224
        val response = MockResponse()
        response.setResponseCode(503)
        response.setBody(jacksonObjectMapper().writeValueAsString(ApiError("002")))
        response.setHeader("Retry-After", estimatedOutage)
        mockWebServer.enqueue(response)

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, "", "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        val exception = assertThrows(BackendMaintenanceException::class.java) {
            okHttpClient.newCall(mockHttpRequest).execute()
        }
        assertThat(exception.estimatedOutage).isEqualTo(estimatedOutage)
    }

    @Test
    fun `should throw a retryable cloud exception when the response code is 500`() {
        val response = MockResponse()
        response.setResponseCode(500)
        mockWebServer.enqueue(response)

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, "", "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        assertThrows(RetryableCloudException::class.java) {
            okHttpClient.newCall(mockHttpRequest).execute()
        }
    }

    @Test
    fun `should throw a retryable cloud exception when the response code is 502`() {
        val response = MockResponse()
        response.setResponseCode(502)
        mockWebServer.enqueue(response)

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, "", "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        assertThrows(RetryableCloudException::class.java) {
            okHttpClient.newCall(mockHttpRequest).execute()
        }
    }

    @Test
    fun `should throw a retryable cloud exception when the response code is 503`() {
        val response = MockResponse()
        response.setResponseCode(503)
        response.setBody(jacksonObjectMapper().writeValueAsString(ApiError("001")))
        mockWebServer.enqueue(response)

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, "", "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        assertThrows(RetryableCloudException::class.java) {
            okHttpClient.newCall(mockHttpRequest).execute()
        }
    }

    @Test
    fun `should throw a sync cloud integration exception when it's 4xx error`() {
        val response = MockResponse()
        response.setResponseCode(400)
        mockWebServer.enqueue(response)

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, "", "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .build()

        // execute mock request
        assertThrows(SyncCloudIntegrationException::class.java) {
            okHttpClient.newCall(mockHttpRequest).execute()
        }
    }
}
