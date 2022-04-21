package com.simprints.id.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.exceptions.safe.BackendMaintenanceException
import com.simprints.id.exceptions.safe.SimprintsInternalServerException
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.SimApiClientFactoryImpl
import com.simprints.id.network.SimApiClientImpl
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.common.retrofit.FakeResponseInterceptor
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config
import retrofit2.HttpException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class AuthenticationDataManagerImplTest : AutoCloseKoinTest() {

    private val nonceFromServer = "nonce_from_server"
    private val publicKeyFromServer = "public_key_from_server"
    private val validAuthenticationJsonResponse = "{\"nonce\":\"$nonceFromServer\", \"publicKey\":\"$publicKeyFromServer\"}"
    private val backendMaintenanceErrorResponse = "{\"error\":\"002\"}"
    private val expectedAuthenticationData = AuthenticationData(Nonce(nonceFromServer), PublicKeyString(publicKeyFromServer))
    private val expectedUrl = DEFAULT_BASE_URL + "projects/$PROJECT_ID/users/$USER_ID/authentication-data?deviceId=$DEVICE_ID"

    @MockK
    lateinit var mockBaseUrlProvider: BaseUrlProvider

    @MockK
    lateinit var mockRemoteDbManager: RemoteDbManager

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    private val validateUrl: (url: String) -> Unit = {
        assertThat(it).isEqualTo(expectedUrl)
    }


    private lateinit var apiClient: SimApiClientImpl<SecureApiInterface>

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this).setupFirebase()
        every { mockBaseUrlProvider.getApiBaseUrl() } returns DEFAULT_BASE_URL
        coEvery { mockRemoteDbManager.getCurrentToken() } returns "token"
        runBlocking {
            apiClient = SimApiClientFactoryImpl(
                mockBaseUrlProvider, "deviceId", "versionName", mockRemoteDbManager, JsonHelper,
                testDispatcherProvider, HttpLoggingInterceptor()
            ).buildClient(SecureApiInterface::class) as SimApiClientImpl<SecureApiInterface>
        }
    }

    @Test
    fun successfulResponse_shouldObtainValidAuthenticationData() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    200,
                    validAuthenticationJsonResponse,
                    validateUrl = validateUrl
                )
            )

            val actualAuthenticationData = makeTestRequestForAuthenticationData()

            assertThat(actualAuthenticationData).isEqualTo(expectedAuthenticationData)
        }
    }

    @Test
    fun receivingAnErrorFromServer_shouldThrowAnException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(500, validateUrl = validateUrl))

            assertThrows<SimprintsInternalServerException> {
                makeTestRequestForAuthenticationData()
            }
        }
    }

    @Test
    fun receivingErrorFromServer_shouldThrowException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    503,
                    "Some response",
                    validateUrl = validateUrl
                )
            )

            assertThrows<SimprintsInternalServerException> {
                makeTestRequestForAuthenticationData()
            }
        }
    }

    @Test
    fun receiving504ErrorFromServer_shouldThrowException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    504,
                    "Some response",
                    validateUrl = validateUrl
                )
            )

            assertThrows<SimprintsInternalServerException> {
                makeTestRequestForAuthenticationData()
            }
        }
    }

    @Test
    fun receiving602ErrorFromServer_shouldThrowException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    602,
                    "Some response",
                    validateUrl = validateUrl
                )
            )

            assertThrows<HttpException> {
                makeTestRequestForAuthenticationData()
            }
        }
    }

    @Test
    fun receiving599ErrorFromServer_shouldThrowException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    599,
                    "Some response",
                    validateUrl = validateUrl
                )
            )

            assertThrows<SimprintsInternalServerException> {
                makeTestRequestForAuthenticationData()
            }
        }
    }

    @Test
    fun receivingABackendErrorFromServer_shouldThrowABackendMaintenanceException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(
                FakeResponseInterceptor(
                    503,
                    backendMaintenanceErrorResponse,
                    validateUrl = validateUrl
                )
            )

            assertThrows<BackendMaintenanceException> {
                makeTestRequestForAuthenticationData()
            }
        }
    }

    private suspend fun makeTestRequestForAuthenticationData(): AuthenticationData {
        val factory = mockk<SimApiClientFactory>()
        every { factory.buildUnauthenticatedClient(SecureApiInterface::class) } returns apiClient
        val authenticationDataManagerSpy = spyk(AuthenticationDataManagerImpl(factory, DEVICE_ID))

        return authenticationDataManagerSpy.requestAuthenticationData(PROJECT_ID, USER_ID)
    }


    companion object {
        private const val PROJECT_ID = "projectId"
        private const val USER_ID = "userId"
        private const val DEVICE_ID = "deviceId"
    }
}
