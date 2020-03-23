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
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.commontesttools.state.setupFakeKeyStore
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
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiToken
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker
import com.simprints.testtools.common.di.DependencyRule.MockkRule
import com.simprints.testtools.common.di.DependencyRule.ReplaceRule
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.getSharedPreferences
import io.mockk.*
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
@ExperimentalCoroutinesApi
class ProjectAuthenticatorImplTest {

    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var remoteSessionsManagerMock: RemoteSessionsManager
    @Inject lateinit var secureDataManager: SecureLocalDbKeyProvider
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var projectRemoteDataSource: ProjectRemoteDataSource
    @Inject lateinit var signerManager: SignerManager
    @Inject lateinit var remoteConfigWrapper: RemoteConfigWrapper
    @Inject lateinit var longConsentRepository: LongConsentRepository
    @Inject lateinit var preferencesManager: PreferencesManager

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private val projectRemoteDataSourceMock: ProjectRemoteDataSource = mockk()
    private val projectLocalDAtaSourceMock: ProjectLocalDataSource = mockk()

    private val appModule by lazy {
        TestAppModule(
            app,
            remoteDbManagerRule = MockkRule,
            loginInfoManagerRule = MockkRule,
            keystoreManagerRule = ReplaceRule { mockk<KeystoreManager>().apply { setupFakeKeyStore(this) } }
        )
    }

    private val dataModule by lazy {
        TestDataModule(
            projectLocalDataSourceRule = ReplaceRule { projectLocalDAtaSourceMock },
            projectRemoteDataSourceRule = ReplaceRule { projectRemoteDataSourceMock }
        )
    }

    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule, dataModule = dataModule).fullSetup()

        runBlocking {
            RobolectricTestMocker
                .initLogInStateMock(getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME), remoteDbManagerMock)
                .mockLoadProject(projectRemoteDataSourceMock, projectLocalDAtaSourceMock)

        }

        coEvery { remoteSessionsManagerMock.getSessionsApiClient() } throws IllegalStateException()

        apiClient = SimApiClientFactory("deviceId", endpoint = BASE_URL).build()
    }

    @Test
    fun successfulResponse_userShouldSignIn() {
        val authenticator = ProjectAuthenticatorImpl(
            SecureApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
            loginInfoManager,
            SafetyNet.getClient(app),
            secureDataManager,
            projectRemoteDataSourceMock,
            signerManager,
            remoteConfigWrapper,
            longConsentRepository,
            preferencesManager,
            getMockAttestationManager()
        )

        runBlockingTest {
            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET)
        }
    }

    @Test(expected = IOException::class)
    fun offline_authenticationShouldThrowException() {
        val nonceScope = NonceScope(PROJECT_ID, USER_ID)

        val authenticator = ProjectAuthenticatorImpl(
            createMockServiceToFailRequests(apiClient.retrofit),
            loginInfoManager,
            SafetyNet.getClient(app),
            secureDataManager,
            projectRemoteDataSource,
            signerManager,
            remoteConfigWrapper,
            longConsentRepository,
            preferencesManager
        )

        runBlockingTest {
            authenticator.authenticate(nonceScope, PROJECT_SECRET)
        }
    }

    @Test
    fun getAuthenticationData_invokeAuthenticationDataManagerCorrectly() {
        val mockAuthenticationDataManager = mockk<AuthenticationDataManager>(relaxed = true)

        val authenticator = ProjectAuthenticatorImpl(
            mockk(),
            loginInfoManager,
            mockk(),
            secureDataManager,
            projectRemoteDataSource,
            signerManager,
            remoteConfigWrapper,
            longConsentRepository,
            preferencesManager,
            authenticationDataManager = mockAuthenticationDataManager
        )

        authenticator.getAuthenticationData(PROJECT_ID, USER_ID)

        verify(exactly = 1) { mockAuthenticationDataManager.requestAuthenticationData(PROJECT_ID, USER_ID) }
    }

    @Test
    fun authenticate_shouldAuthenticateWithRightRequestsToSecureApiInterface() {
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
            projectRemoteDataSource,
            signerManager,
            remoteConfigWrapper,
            longConsentRepository,
            preferencesManager,
            getMockAttestationManager()
        ))

        every { authenticatorSpy.projectSecretManager } returns mockProjectSecretManager

        runBlockingTest {
            authenticatorSpy.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET)
        }

        assertThat(mockWebServer.requestCount).isEqualTo(2)
    }

    @Test(expected = SafetyNetException::class)
    fun safetyNetFailed_shouldThrowRightException() {
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
            projectRemoteDataSource,
            signerManager,
            remoteConfigWrapper,
            longConsentRepository,
            preferencesManager,
            mockAttestationManager
        )

        runBlockingTest {
            authenticator.authenticate(nonceScope, PROJECT_SECRET)
        }
    }

    private fun getMockAttestationManager(): AttestationManager {
        val mockAttestationManager = mockk<AttestationManager>()
        every {
            mockAttestationManager.requestAttestation(any(), any())
        } returns Single.just(AttestToken("google_attestation"))

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
