package com.simprints.id.secure.securitystate.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.network.SimApiClientImpl
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.tools.performance.PerformanceMonitoringHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class SecurityStateRemoteDataSourceImplTest {

    @MockK lateinit var mockLoginInfoManager: LoginInfoManager
    @MockK lateinit var mockPerformanceMonitoringHelper: PerformanceMonitoringHelper

    private lateinit var remoteDataSource: SecurityStateRemoteDataSourceImpl

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        val mockFactory = mockk<SimApiClientFactory>()
        coEvery {
            mockFactory.buildClient(SecureApiInterface::class)
        } returns SimApiClientImpl(
            SecureApiInterface::class,
            mockWebServer.url("/").toString(),
            DEVICE_ID,
            mockPerformanceMonitoringHelper
        )

        every { mockLoginInfoManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID

        remoteDataSource = SecurityStateRemoteDataSourceImpl(
            mockFactory,
            mockLoginInfoManager,
            DEVICE_ID
        )
    }

    @Test
    fun withSuccessfulResponse_shouldGetSecurityState() = runBlocking {
        mockSuccessfulResponse()

        val response = remoteDataSource.getSecurityState()

        assertThat(response).isNotNull()
    }

    @Test(expected = SimprintsInternalServerException::class)
    fun withErrorResponseCodeBetween500And599_shouldThrowInternalServerException() {
        mockErrorResponse(501)

        runBlocking {
            remoteDataSource.getSecurityState()
        }
    }

    @Test(expected = HttpException::class)
    fun withErrorResponseCode_shouldThrowHttpException() {
        mockErrorResponse(404)

        runBlocking {
            remoteDataSource.getSecurityState()
        }
    }

    private fun mockSuccessfulResponse() {
        // TODO: replace empty JSON with SecurityState
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
    }

    private fun mockErrorResponse(code: Int) {
        mockWebServer.enqueue(MockResponse().setResponseCode(code))
    }

    private companion object {
        const val PROJECT_ID = "mock-project-id"
        const val DEVICE_ID = "mock-device-id"
    }

}
