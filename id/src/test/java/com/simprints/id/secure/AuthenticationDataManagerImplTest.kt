package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.secure.models.remote.ApiAuthenticationData
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
class AuthenticationDataManagerImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val USER_ID = "userId"
        private const val DEVICE_ID = "deviceId"
        private const val NONCE = "nonce_from_server"
        private const val PUBLIC_KEY = "public_key_from_server"
    }

    private val remoteInterface = mockk<SecureApiInterface>()
    private val simApiClient = mockk<SimApiClient<SecureApiInterface>>()
    private val simApiClientFactory = mockk<SimApiClientFactory>()
    private val authenticationDataManagerImpl =
        AuthenticationDataManagerImpl(simApiClientFactory, DEVICE_ID)

    @Before
    fun setUp() {
        coEvery { simApiClient.executeCall<ApiAuthenticationData>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<SecureApiInterface, ApiAuthenticationData>).invoke(
                remoteInterface
            )
        }
        coEvery { simApiClientFactory.buildUnauthenticatedClient(SecureApiInterface::class) } returns simApiClient
    }

    @Test
    fun `Get successful authentication data`() =
        runTest(StandardTestDispatcher()) {
            coEvery {
                remoteInterface.requestAuthenticationData(
                    PROJECT_ID,
                    USER_ID,
                    DEVICE_ID
                )
            } returns ApiAuthenticationData(NONCE, PUBLIC_KEY)

            val actualAuthenticationData =
                authenticationDataManagerImpl.requestAuthenticationData(PROJECT_ID, USER_ID)
            val expectedAuthenticationData =
                AuthenticationData(Nonce(NONCE), PublicKeyString(PUBLIC_KEY))
            assertThat(actualAuthenticationData).isEqualTo(expectedAuthenticationData)
        }

    @Test
    fun `Get no authentication data if backend maintenance exception`() =
        runTest(StandardTestDispatcher()) {
            val exception = BackendMaintenanceException(estimatedOutage = 100)
            coEvery {
                remoteInterface.requestAuthenticationData(
                    PROJECT_ID,
                    USER_ID,
                    DEVICE_ID
                )
            } throws exception

            val receivedException = assertThrows<BackendMaintenanceException> {
                authenticationDataManagerImpl.requestAuthenticationData(PROJECT_ID, USER_ID)
            }
            assertThat(receivedException).isEqualTo(exception)
        }

    @Test
    fun `Get no authentication data if sync cloud integration exception`() =
        runTest(StandardTestDispatcher()) {
            val exception = SyncCloudIntegrationException(cause = Exception())
            coEvery {
                remoteInterface.requestAuthenticationData(
                    PROJECT_ID,
                    USER_ID,
                    DEVICE_ID
                )
            } throws exception

            val receivedException = assertThrows<SyncCloudIntegrationException> {
                authenticationDataManagerImpl.requestAuthenticationData(PROJECT_ID, USER_ID)
            }
            assertThat(receivedException).isEqualTo(exception)
        }

    @Test
    fun `Should map the sync cloud integration maintenance exception to AuthRequestInvalidCredentialsException when the response is 404`() =
        runTest(StandardTestDispatcher()) {
            val exception = SyncCloudIntegrationException(
                cause = HttpException(
                    Response.error<ApiAuthenticationData>(
                        404,
                        "".toResponseBody()
                    )
                )
            )
            coEvery {
                remoteInterface.requestAuthenticationData(
                    PROJECT_ID,
                    USER_ID,
                    DEVICE_ID
                )
            } throws exception

            assertThrows<AuthRequestInvalidCredentialsException> {
                authenticationDataManagerImpl.requestAuthenticationData(PROJECT_ID, USER_ID)
            }
        }

}
