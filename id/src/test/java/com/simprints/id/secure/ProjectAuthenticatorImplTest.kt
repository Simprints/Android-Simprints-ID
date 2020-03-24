@file:Suppress("DEPRECATION")

package com.simprints.id.secure

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.safetynet.SafetyNet
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.NetworkConstants.Companion.BASE_URL
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.session.remote.RemoteSessionsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiToken
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.getSharedPreferences
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
@ExperimentalCoroutinesApi
class ProjectAuthenticatorImplTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    @MockK private lateinit var projectRemoteDataSourceMock: ProjectRemoteDataSource 
    private val projectLocalDataSourceMock: ProjectLocalDataSource = mockk()
    private val longConsentRepositoryMock: LongConsentRepository = mockk()
    private val secureDataManager: SecureLocalDbKeyProvider = mockk()
    private val loginInfoManager: LoginInfoManager = mockk()
    private val remoteDbManagerMock: RemoteDbManager = mockk()
    private val remoteSessionsManagerMock: RemoteSessionsManager = mockk()
    private val signerManager: SignerManager = mockk()
    private val remoteConfigWrapper: RemoteConfigWrapper = mockk()
    private val preferencesManager: PreferencesManager = mockk()

    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Before
    fun setUp() {
        UnitTestConfig(this).setupFirebase().coroutinesMainThread()
        runBlocking {
            RobolectricTestMocker
                .initLogInStateMock(getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME), remoteDbManagerMock)
                .mockLoadProject(projectRemoteDataSourceMock, projectLocalDataSourceMock)
        }

        coEvery { remoteSessionsManagerMock.getSessionsApiClient() } throws IllegalStateException()

        apiClient = SimApiClientFactory("deviceId", endpoint = BASE_URL).build()
    }

    @Test
    fun successfulResponse_userShouldSignIn() {
        runBlocking {
            val authenticator = ProjectAuthenticatorImpl(
                SecureApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
                loginInfoManager,
                SafetyNet.getClient(app),
                secureDataManager,
                projectRemoteDataSourceMock,
                signerManager,
                remoteConfigWrapper,
                longConsentRepositoryMock,
                preferencesManager,
                getMockAttestationManager()
            )

            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET)


        }
    }

    @Test(expected = IOException::class)
    fun offline_authenticationShouldThrowException() {
        runBlocking {
            val nonceScope = NonceScope(PROJECT_ID, USER_ID)
            val mockService = createMockServiceToFailRequests(apiClient.retrofit)
            println("ProjectAuthenticator: Authenticator $mockService")
            val authenticator = ProjectAuthenticatorImpl(
                mockService,
                loginInfoManager,
                SafetyNet.getClient(app),
                secureDataManager,
                projectRemoteDataSourceMock,
                signerManager,
                remoteConfigWrapper,
                longConsentRepositoryMock,
                preferencesManager
            )

            authenticator.authenticate(nonceScope, PROJECT_SECRET)
        }
    }

    @Test
    fun getAuthenticationData_invokeAuthenticationDataManagerCorrectly() {
        runBlocking {
            val mockAuthenticationDataManager = mockk<AuthenticationDataManager>(relaxed = true)

            val authenticator = ProjectAuthenticatorImpl(
                mockk(),
                loginInfoManager,
                mockk(),
                secureDataManager,
                projectRemoteDataSourceMock,
                signerManager,
                remoteConfigWrapper,
                longConsentRepositoryMock,
                preferencesManager,
                authenticationDataManager = mockAuthenticationDataManager
            )
            authenticator.getAuthenticationData(PROJECT_ID, USER_ID)

            coVerify(exactly = 1) { mockAuthenticationDataManager.requestAuthenticationData(PROJECT_ID, USER_ID) }
        }
    }

    @Test
    fun authenticate_shouldAuthenticateWithRightRequestsToSecureApiInterface() {
        runBlocking {
            val mockWebServer = MockWebServer()
            val mockProjectSecretManager: ProjectSecretManager = mockk()

            every {
                mockProjectSecretManager.encryptAndStoreAndReturnProjectSecret(any(), any())
            } returns PROJECT_SECRET

            with(mockWebServer) {
                enqueue(mockResponseForAuthenticationData())
                enqueue(mockResponseForApiToken())
            }

            val authenticatorSpy = spyk(ProjectAuthenticatorImpl(
                SimApiClientFactory("deviceId", mockWebServer.url("/").toString()).build<SecureApiInterface>().api,
                loginInfoManager,
                SafetyNet.getClient(app),
                secureDataManager,
                projectRemoteDataSourceMock,
                signerManager,
                remoteConfigWrapper,
                longConsentRepositoryMock,
                preferencesManager,
                getMockAttestationManager()
            ))

            every { authenticatorSpy.projectSecretManager } returns mockProjectSecretManager

            authenticatorSpy.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET)

            assertThat(mockWebServer.requestCount).isEqualTo(2)
        }
    }

    @Test(expected = SafetyNetException::class)
    fun safetyNetFailed_shouldThrowRightException() {
        runBlocking {
            val mockAttestationManager = mockk<AttestationManager>()
            val nonceScope = NonceScope(PROJECT_ID, USER_ID)

            every {
                mockAttestationManager.requestAttestation(any(), any())
            } throws (SafetyNetException(reason = SafetyNetExceptionReason.SERVICE_UNAVAILABLE))

            val authenticator = ProjectAuthenticatorImpl(
                SecureApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
                loginInfoManager,
                SafetyNet.getClient(app),
                secureDataManager,
                projectRemoteDataSourceMock,
                signerManager,
                remoteConfigWrapper,
                longConsentRepositoryMock,
                preferencesManager,
                mockAttestationManager
            )

            authenticator.authenticate(nonceScope, PROJECT_SECRET)
        }
    }

    private fun getMockAttestationManager(): AttestationManager {
        val mockAttestationManager = mockk<AttestationManager>()
        every {
            mockAttestationManager.requestAttestation(any(), any())
        } returns AttestToken("google_attestation")

        return mockAttestationManager
    }

    private fun mockResponseForAuthenticationData() = with(MockResponse()) {
        setResponseCode(200)
        setBody(JsonHelper.toJson(ApiAuthenticationData("nonce", "publicKeyString")))
    }

    private fun mockResponseForApiToken() = with(MockResponse()) {
        setResponseCode(200)
        setBody(JsonHelper.toJson(ApiToken("firebaseCustomToken")))
    }

    private companion object {
        const val PROJECT_ID = "project_id"
        const val USER_ID = "user_id"
        const val PROJECT_SECRET = "encrypted_project_secret"
    }

}
