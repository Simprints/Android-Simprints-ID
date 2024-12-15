package com.simprints.infra.authlogic.authenticator.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authlogic.authenticator.remote.models.ApiAuthRequestBody
import com.simprints.infra.authlogic.authenticator.remote.models.ApiAuthenticationData
import com.simprints.infra.authlogic.authenticator.remote.models.ApiToken
import com.simprints.infra.authstore.domain.models.AuthRequest
import com.simprints.infra.authstore.domain.models.AuthenticationData
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.alias.InterfaceInvocation
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AuthenticationRemoteDataSourceTest {
    companion object {
        private const val PROJECT_ID = "projectId"
        private const val DEVICE_ID = "deviceId"
        private const val NONCE = "nonce_from_server"
        private val AUTH_REQUEST = AuthRequest("secret", "integrityToken")
        private val API_AUTH_REQUEST_BODY = ApiAuthRequestBody("secret", "integrityToken")
    }

    private val remoteInterface = mockk<AuthenticationRemoteInterface>()
    private val simApiClient = mockk<SimNetwork.SimApiClient<AuthenticationRemoteInterface>>()
    private val simApiClientFactory = mockk<UnauthenticatedClientFactory>()
    private val authenticationRemoteDataSource = AuthenticationRemoteDataSource(simApiClientFactory)

    @Before
    fun setUp() {
        coEvery { simApiClient.executeCall<ApiAuthenticationData>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<AuthenticationRemoteInterface, ApiAuthenticationData>).invoke(
                remoteInterface,
            )
        }
        coEvery { simApiClientFactory.build(AuthenticationRemoteInterface::class) } returns simApiClient
    }

    @Test
    fun `Get successful authentication data`() = runTest(StandardTestDispatcher()) {
        coEvery {
            remoteInterface.requestAuthenticationData(
                PROJECT_ID,
                DEVICE_ID,
            )
        } returns ApiAuthenticationData(NONCE)

        val actualAuthenticationData =
            authenticationRemoteDataSource.requestAuthenticationData(
                PROJECT_ID,
                DEVICE_ID,
            )
        val expectedAuthenticationData = AuthenticationData(NONCE)
        assertThat(actualAuthenticationData).isEqualTo(expectedAuthenticationData)
    }

    @Test
    fun `Get no authentication data if backend maintenance exception`() = runTest(StandardTestDispatcher()) {
        val exception = BackendMaintenanceException(estimatedOutage = 100)
        coEvery {
            remoteInterface.requestAuthenticationData(
                PROJECT_ID,
                DEVICE_ID,
            )
        } throws exception

        val receivedException = assertThrows<BackendMaintenanceException> {
            authenticationRemoteDataSource.requestAuthenticationData(
                PROJECT_ID,
                DEVICE_ID,
            )
        }
        assertThat(receivedException).isEqualTo(exception)
    }

    @Test
    fun `Get no authentication data if sync cloud integration exception`() = runTest(StandardTestDispatcher()) {
        val exception = SyncCloudIntegrationException(cause = Exception())
        coEvery {
            remoteInterface.requestAuthenticationData(
                PROJECT_ID,
                DEVICE_ID,
            )
        } throws exception

        val receivedException = assertThrows<SyncCloudIntegrationException> {
            authenticationRemoteDataSource.requestAuthenticationData(
                PROJECT_ID,
                DEVICE_ID,
            )
        }
        assertThat(receivedException).isEqualTo(exception)
    }

    @Test
    fun `Get authentication data should map the sync cloud integration maintenance exception to AuthRequestInvalidCredentialsException when the response is 404`() =
        runTest(StandardTestDispatcher()) {
            val exception = SyncCloudIntegrationException(
                cause = HttpException(
                    Response.error<ApiAuthenticationData>(
                        404,
                        "".toResponseBody(),
                    ),
                ),
            )
            coEvery {
                remoteInterface.requestAuthenticationData(
                    PROJECT_ID,
                    DEVICE_ID,
                )
            } throws exception

            assertThrows<AuthRequestInvalidCredentialsException> {
                authenticationRemoteDataSource.requestAuthenticationData(
                    PROJECT_ID,
                    DEVICE_ID,
                )
            }
        }

    @Test
    fun `Get successful auth token`() = runTest(StandardTestDispatcher()) {
        val apiToken = ApiToken(
            "token",
            ApiToken.FirebaseOptions("project", "api", "application", "url", "sender", "bucket"),
        )
        coEvery {
            remoteInterface.requestCustomTokens(PROJECT_ID, DEVICE_ID, API_AUTH_REQUEST_BODY)
        } returns apiToken

        val actualToken = authenticationRemoteDataSource.requestAuthToken(
            PROJECT_ID,
            DEVICE_ID,
            AUTH_REQUEST,
        )
        val expectedToken = Token("token", "project", "api", "application")
        assertThat(actualToken).isEqualTo(expectedToken)
    }

    @Test
    fun `Get no auth token if backend maintenance exception`() = runTest(StandardTestDispatcher()) {
        val exception = BackendMaintenanceException(estimatedOutage = 100)
        coEvery {
            remoteInterface.requestCustomTokens(PROJECT_ID, DEVICE_ID, API_AUTH_REQUEST_BODY)
        } throws exception

        val receivedException = assertThrows<BackendMaintenanceException> {
            authenticationRemoteDataSource.requestAuthToken(
                PROJECT_ID,
                DEVICE_ID,
                AUTH_REQUEST,
            )
        }
        assertThat(receivedException).isEqualTo(exception)
    }

    @Test
    fun `Get no auth token if sync cloud integration exception`() = runTest(StandardTestDispatcher()) {
        val exception = SyncCloudIntegrationException(cause = Exception())
        coEvery {
            remoteInterface.requestCustomTokens(PROJECT_ID, DEVICE_ID, API_AUTH_REQUEST_BODY)
        } throws exception

        val receivedException = assertThrows<SyncCloudIntegrationException> {
            authenticationRemoteDataSource.requestAuthToken(
                PROJECT_ID,
                DEVICE_ID,
                AUTH_REQUEST,
            )
        }
        assertThat(receivedException).isEqualTo(exception)
    }

    @Test
    fun `Get auth token should map the sync cloud integration maintenance exception to AuthRequestInvalidCredentialsException when the response is 401`() =
        runTest(StandardTestDispatcher()) {
            val exception = SyncCloudIntegrationException(
                cause = HttpException(
                    Response.error<ApiToken>(
                        401,
                        "".toResponseBody(),
                    ),
                ),
            )
            coEvery {
                remoteInterface.requestCustomTokens(PROJECT_ID, DEVICE_ID, API_AUTH_REQUEST_BODY)
            } throws exception

            assertThrows<AuthRequestInvalidCredentialsException> {
                authenticationRemoteDataSource.requestAuthToken(
                    PROJECT_ID,
                    DEVICE_ID,
                    AUTH_REQUEST,
                )
            }
        }
}
