package com.simprints.id.secure.securitystate.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.SimprintsInternalServerException
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.network.SimApiClientImpl
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.models.SecurityState
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException

@RunWith(AndroidJUnit4::class)
class SecurityStateRemoteDataSourceImplTest {

    @MockK lateinit var mockLoginInfoManager: LoginInfoManager

    private lateinit var remoteDataSource: SecurityStateRemoteDataSourceImpl

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)
        mockkStatic("com.simprints.id.tools.extensions.PerformanceMonitoring_extKt")

        val mockFactory = mockk<SimApiClientFactory>()
        coEvery {
            mockFactory.buildClient(SecureApiInterface::class)
        } returns SimApiClientImpl(
            SecureApiInterface::class,
            mockWebServer.url("/").toString(),
            DEVICE_ID,
            VERSION_NAME,
            randomUUID(),
            mockk(),
            JsonHelper()
        )

        every { mockLoginInfoManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID

        remoteDataSource = SecurityStateRemoteDataSourceImpl(
            mockFactory,
            mockLoginInfoManager,
            DEVICE_ID
        )
    }

    @Test
    @Ignore("API call is commented out until backend implements security state endpoint")
    fun withSuccessfulResponse_shouldGetSecurityState() = runBlocking {
        mockSuccessfulResponse()

        val response = remoteDataSource.getSecurityState()
        val expected = SecurityState(DEVICE_ID, SecurityState.Status.COMPROMISED)

        assertThat(response).isEqualTo(expected)
    }

    @Test(expected = SimprintsInternalServerException::class)
    @Ignore("API call is commented out until backend implements security state endpoint")
    fun withErrorResponseCodeBetween500And599_shouldThrowInternalServerException() {
        mockErrorResponse(501)

        runBlocking {
            remoteDataSource.getSecurityState()
        }
    }

    @Test(expected = HttpException::class)
    @Ignore("API call is commented out until backend implements security state endpoint")
    fun withErrorResponseCode_shouldThrowHttpException() {
        mockErrorResponse(404)

        runBlocking {
            remoteDataSource.getSecurityState()
        }
    }

    private fun mockSuccessfulResponse() {
        val json = "{ \"deviceId\": \"$DEVICE_ID\", \"status\": \"COMPROMISED\" }"
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))
    }

    private fun mockErrorResponse(code: Int) {
        mockWebServer.enqueue(MockResponse().setResponseCode(code))
    }

    private companion object {
        const val PROJECT_ID = "mock-project-id"
        const val DEVICE_ID = "mock-device-id"
        const val VERSION_NAME = "mock-version-name"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
