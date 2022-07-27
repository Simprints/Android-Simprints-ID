package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.AuthRequestBody
import com.simprints.id.secure.models.Token
import com.simprints.id.secure.models.remote.ApiToken
import com.simprints.infra.network.SimApiClient
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.alias.InterfaceInvocation
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class AuthManagerImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val USER_ID = "userId"
        private val REQUEST_BODY = AuthRequestBody("secret", "safety", "deviceId")
        private val AUTH_REQUEST = AuthRequest(PROJECT_ID, USER_ID, REQUEST_BODY)
    }

    private val remoteInterface = mockk<SecureApiInterface>()
    private val simApiClient = mockk<SimApiClient<SecureApiInterface>>()
    private val simApiClientFactory = mockk<SimApiClientFactory>()
    private val authManagerImpl = AuthManagerImpl(simApiClientFactory)

    @Before
    fun setup() {
        coEvery { simApiClient.executeCall<ApiToken>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<SecureApiInterface, ApiToken>).invoke(
                remoteInterface
            )
        }
        coEvery { simApiClientFactory.buildUnauthenticatedClient(SecureApiInterface::class) } returns simApiClient
    }

    @Test
    fun `Get successful authentication data`() =
        runTest(StandardTestDispatcher()) {
            val apiToken = ApiToken(
                "token",
                ApiToken.FirebaseOptions("project", "api", "application", "url", "sender", "bucket")
            )
            coEvery {
                remoteInterface.requestCustomTokens(PROJECT_ID, USER_ID, REQUEST_BODY)
            } returns apiToken

            val actualToken = authManagerImpl.requestAuthToken(AUTH_REQUEST)
            val expectedToken = Token("token", "project", "api", "application")
            assertThat(actualToken).isEqualTo(expectedToken)
        }

    @Test
    fun `Get no authentication data if backend maintenance exception`() =
        runTest(StandardTestDispatcher()) {
            val exception = BackendMaintenanceException(estimatedOutage = 100)
            coEvery {
                remoteInterface.requestCustomTokens(PROJECT_ID, USER_ID, REQUEST_BODY)
            } throws exception

            val receivedException = assertThrows<BackendMaintenanceException> {
                authManagerImpl.requestAuthToken(AUTH_REQUEST)
            }
            assertThat(receivedException).isEqualTo(exception)
        }

    @Test
    fun `Get no authentication data if sync cloud integration exception`() =
        runTest(StandardTestDispatcher()) {
            val exception = SyncCloudIntegrationException(cause = Exception())
            coEvery {
                remoteInterface.requestCustomTokens(PROJECT_ID, USER_ID, REQUEST_BODY)
            } throws exception

            val receivedException = assertThrows<SyncCloudIntegrationException> {
                authManagerImpl.requestAuthToken(AUTH_REQUEST)
            }
            assertThat(receivedException).isEqualTo(exception)
        }

    @Test
    fun `Should map the sync cloud integration maintenance exception to AuthRequestInvalidCredentialsException when the response is 404`() =
        runTest(StandardTestDispatcher()) {
            val exception = SyncCloudIntegrationException(
                cause = HttpException(
                    Response.error<ApiToken>(
                        401,
                        "".toResponseBody()
                    )
                )
            )
            coEvery {
                remoteInterface.requestCustomTokens(PROJECT_ID, USER_ID, REQUEST_BODY)
            } throws exception

            assertThrows<AuthRequestInvalidCredentialsException> {
                authManagerImpl.requestAuthToken(AUTH_REQUEST)
            }
        }
//    @Test
//    fun receivingABackendErrorFromServer_shouldThrowABackendMaintenanceException() {
//        runBlocking {
//            apiClient.okHttpClientConfig.addInterceptor(
//                FakeResponseInterceptor(
//                    503,
//                    backendMaintenanceErrorResponse
//                )
//            )
//
//            assertThrows<BackendMaintenanceException> {
//                makeTestRequestForTokenData()
//            }
//        }
//    }
//
//    @Test
//    fun receiving503ErrorFromServer_shouldThrowServerException() {
//        runBlocking {
//            apiClient.okHttpClientConfig.addInterceptor(
//                FakeResponseInterceptor(
//                    500,
//                    "backendMaintenanceErrorResponse"
//                )
//            )
//
//            assertThrows<SimprintsInternalServerException> {
//                makeTestRequestForTokenData()
//            }
//        }
//    }
//
//    @Test
//    fun receiving599ErrorFromServer_shouldThrowServerException() {
//        runBlocking {
//            apiClient.okHttpClientConfig.addInterceptor(
//                FakeResponseInterceptor(
//                    599,
//                    "backendMaintenanceErrorResponse"
//                )
//            )
//
//            assertThrows<SimprintsInternalServerException> {
//                makeTestRequestForTokenData()
//            }
//        }
//    }
//
//    @Test
//    fun receiving504ErrorFromServer_shouldThrowInternalServerException() {
//        runBlocking {
//            apiClient.okHttpClientConfig.addInterceptor(
//                FakeResponseInterceptor(
//                    504,
//                    "backendMaintenanceErrorResponse"
//                )
//            )
//
//            assertThrows<SimprintsInternalServerException> {
//                makeTestRequestForTokenData()
//            }
//        }
//    }
//
//    @Test
//    fun receivingErrorFromServer_shouldThrowHttpException() {
//        runBlocking {
//            apiClient.okHttpClientConfig.addInterceptor(
//                FakeResponseInterceptor(
//                    602,
//                    "backendMaintenanceErrorResponse"
//                )
//            )
//
//            assertThrows<HttpException> {
//                makeTestRequestForTokenData()
//            }
//        }
//    }
//
//    private suspend fun makeTestRequestForTokenData(): Token {
//        val factory = mockk<SimApiClientFactory>()
//        every { factory.buildUnauthenticatedClient(SecureApiInterface::class) } returns apiClient
//        val authManagerSpy = spyk(AuthManagerImpl(factory))
//
//        return authManagerSpy.requestAuthToken(authRequest)
//    }
}
