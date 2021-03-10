package com.simprints.id.network

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class HttpInterceptorTest {

    // mock server to read http request parameters
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        // setup & start mock server with enqueued mock response
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockWebServer.enqueue(MockResponse())
    }


    @Test
    fun `should include SID version in request headers`() {
        val versionName = "cxxlvxzn.12.2049"

        // create okHttp client using default builder
        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get("", "", versionName)
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
        val deviceId = "symeAwxomedyvexeid"

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get("", deviceId, "")
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
        val okHttpBuilder = DefaultOkHttpClientBuilder()

        okHttpClient = okHttpBuilder
            .get(null, "", "")
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
        val authToken = "eyxSomeAwesomeAuth.TokenThatIsUsed.ForUnitTesting"

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(authToken, "", "")
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

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
