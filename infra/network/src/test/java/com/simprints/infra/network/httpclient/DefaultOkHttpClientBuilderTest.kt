package com.simprints.infra.network.httpclient

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
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
    fun `should not compress request body if gzip header not provided`() {
        mockWebServer.enqueue(MockResponse())

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, null, "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .url(mockWebServer.url("/"))
            .post("some request body".toRequestBody("text/plain".toMediaTypeOrNull()))
            .build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.body.readUtf8()).isEqualTo("some request body")
    }

    @Test
    fun `should compress request body if gzip header provided`() {
        mockWebServer.enqueue(MockResponse())

        val okHttpBuilder = DefaultOkHttpClientBuilder()
        okHttpClient = okHttpBuilder
            .get(ctx, null, "", "")
            .build()

        val mockHttpRequest = Request.Builder()
            .header("Content-Encoding", "gzip")
            .url(mockWebServer.url("/"))
            .post("some request body".toRequestBody("text/plain".toMediaTypeOrNull()))
            .build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.body.readUtf8()).isNotEqualTo("some request body")
    }
}
