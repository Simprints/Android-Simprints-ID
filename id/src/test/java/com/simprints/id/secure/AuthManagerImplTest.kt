package com.simprints.id.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.network.NetworkConstants
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.exceptions.safe.BackendMaintenanceException
import com.simprints.id.exceptions.safe.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.SimApiClientFactoryImpl
import com.simprints.id.network.SimApiClientImpl
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Token
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.common.retrofit.FakeResponseInterceptor
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import retrofit2.HttpException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class AuthManagerImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    private lateinit var authManagerImpl: AuthManagerImpl
    private val simApiClientFactory: SimApiClientFactory = mockk(relaxed = true)
    private val authRequest: AuthRequest = mockk(relaxed = true)
    private lateinit var apiClient: SimApiClientImpl<SecureApiInterface>
    private val mockBaseUrlProvider: BaseUrlProvider = mockk(relaxed = true)
    private val mockRemoteDbManager: RemoteDbManager = mockk(relaxed = true)
    private val backendMaintenanceErrorResponse = "{\"error\":\"002\"}"

    @Before
    fun setup() {
        authManagerImpl = AuthManagerImpl(simApiClientFactory)
        every { mockBaseUrlProvider.getApiBaseUrl() } returns NetworkConstants.DEFAULT_BASE_URL
        coEvery { mockRemoteDbManager.getCurrentToken() } returns "token"
        runBlocking {
            apiClient = SimApiClientFactoryImpl(
                mockBaseUrlProvider, "deviceId", "versionName", mockRemoteDbManager, JsonHelper,
                testDispatcherProvider, HttpLoggingInterceptor()
            ).buildClient(SecureApiInterface::class) as SimApiClientImpl<SecureApiInterface>
        }
    }

    @Test
    fun receivingABackendErrorFromServer_shouldThrowABackendMaintenanceException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    503,
                    backendMaintenanceErrorResponse
                )
            )

            assertThrows<BackendMaintenanceException> {
                makeTestRequestForTokenData()
            }
        }
    }

    @Test
    fun receiving503ErrorFromServer_shouldThrowServerException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    500,
                    "backendMaintenanceErrorResponse"
                )
            )

            assertThrows<SimprintsInternalServerException> {
                makeTestRequestForTokenData()
            }
        }
    }

    @Test
    fun receiving504ErrorFromServer_shouldThrowInternalServerException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    504,
                    "backendMaintenanceErrorResponse"
                )
            )

            assertThrows<SimprintsInternalServerException> {
                makeTestRequestForTokenData()
            }
        }
    }

    @Test
    fun receivingErrorFromServer_shouldThrowHttpException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    602,
                    "backendMaintenanceErrorResponse"
                )
            )

            assertThrows<HttpException> {
                makeTestRequestForTokenData()
            }
        }
    }

    private suspend fun makeTestRequestForTokenData(): Token {
        val factory = mockk<SimApiClientFactory>()
        every { factory.buildUnauthenticatedClient(SecureApiInterface::class) } returns apiClient
        val authManagerSpy = spyk(AuthManagerImpl(factory))

        return authManagerSpy.requestAuthToken(authRequest)
    }
}
