package com.simprints.infra.network.httpclient

import android.content.Context
import com.google.common.truth.Truth.*
import com.simprints.infra.network.httpclient.BuildOkHttpClientUseCase.Companion.AUTHORIZATION_HEADER
import com.simprints.infra.network.httpclient.BuildOkHttpClientUseCase.Companion.DEVICE_ID_HEADER
import com.simprints.infra.network.httpclient.BuildOkHttpClientUseCase.Companion.USER_AGENT_HEADER
import com.simprints.logging.persistent.PersistentLogger
import io.mockk.*
import io.mockk.impl.annotations.MockK
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class BuildOkHttpClientUseCaseTest {
    // mock server to read http request parameters
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @MockK
    lateinit var ctx: Context

    @MockK
    lateinit var networkCache: okhttp3.Cache

    @MockK
    lateinit var persistentLogger: PersistentLogger

    private lateinit var buildOkHttpClient: BuildOkHttpClientUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        mockWebServer = MockWebServer()
        mockWebServer.start()

        buildOkHttpClient = BuildOkHttpClientUseCase(ctx, networkCache, persistentLogger)
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
        okHttpClient = buildOkHttpClient("", "", versionName)

        val mockHttpRequest = Request.Builder().url(mockWebServer.url("/")).build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.getHeader(USER_AGENT_HEADER))
            .isEqualTo("SimprintsID/$versionName")
    }

    @Test
    fun `should include provided device ID in request headers`() {
        mockWebServer.enqueue(MockResponse())

        val deviceId = "symeAwxomedyvexeid"

        okHttpClient = buildOkHttpClient("", deviceId, "")

        val mockHttpRequest = Request.Builder().url(mockWebServer.url("/")).build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.getHeader(DEVICE_ID_HEADER))
            .isEqualTo(deviceId)
    }

    @Test
    fun `should not include auth token in request headers, when auth token is null`() {
        mockWebServer.enqueue(MockResponse())

        okHttpClient = buildOkHttpClient(null, "", "")

        val mockHttpRequest = Request.Builder().url(mockWebServer.url("/")).build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.getHeader(AUTHORIZATION_HEADER)).isNull()
    }

    @Test
    fun `should include provided auth token in request headers`() {
        mockWebServer.enqueue(MockResponse())

        val authToken = "eyxSomeAwesomeAuth.TokenThatIsUsed.ForUnitTesting"

        okHttpClient = buildOkHttpClient(authToken, "", "")

        val mockHttpRequest = Request.Builder().url(mockWebServer.url("/")).build()

        // execute mock request
        okHttpClient.newCall(mockHttpRequest).execute()
        // read recorded request
        val recordedRequest = mockWebServer.takeRequest()

        assertThat(recordedRequest.getHeader(AUTHORIZATION_HEADER))
            .isEqualTo("Bearer $authToken")
    }

    @Test
    fun `should return same instance for same token`() {
        val client1 = buildOkHttpClient(authToken = "token123", deviceId = "12345", versionName = "1.0")
        val client2 = buildOkHttpClient(authToken = "token123", deviceId = "12345", versionName = "1.0")

        assertThat(client1).isSameInstanceAs(client2)
    }

    @Test
    fun `should create new client when authToken changes`() {
        val client1 = buildOkHttpClient(authToken = null, deviceId = "12345", versionName = "1.0")
        val client2 = buildOkHttpClient(authToken = "token2", deviceId = "12345", versionName = "1.0")
        val client3 = buildOkHttpClient(authToken = "token3", deviceId = "12345", versionName = "1.0")

        assertThat(client1).isNotSameInstanceAs(client2)
        assertThat(client2).isNotSameInstanceAs(client3)
        assertThat(client1).isNotSameInstanceAs(client3)
    }

    @Test
    fun `should not compress request body if gzip header not provided`() {
        mockWebServer.enqueue(MockResponse())

        okHttpClient = buildOkHttpClient(null, "", "")

        val mockHttpRequest =
            Request
                .Builder()
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

        okHttpClient = buildOkHttpClient(null, "", "")

        val mockHttpRequest = Request
            .Builder()
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
