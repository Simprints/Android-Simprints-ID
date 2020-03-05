package com.simprints.id.secure

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.safetynet.SafetyNet
import com.google.common.truth.Truth
import com.simprints.core.network.NetworkConstants.Companion.BASE_URL
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.commontesttools.state.setupFakeKeyStore
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.session.remote.RemoteSessionsManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiToken
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
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
import kotlinx.coroutines.runBlocking
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
class ProjectAuthenticatorImplTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    private val projectRemoteDataSourceMock: ProjectRemoteDataSource = mockk()
    private val projectLocalDAtaSourceMock: ProjectLocalDataSource = mockk()

    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var remoteSessionsManagerMock: RemoteSessionsManager
    @Inject lateinit var peopleSyncManagerMock: PeopleSyncManager

    private val projectId = "project_id"
    private val userId = "user_id"

    private val module by lazy {
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

    @Before
    fun setUp() {
        UnitTestConfig(this, module, dataModule = dataModule).fullSetup()

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
            app.component,
            SafetyNet.getClient(app),
            SecureApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
            getMockAttestationManager())

        val testObserver = authenticator
            .authenticate(NonceScope(projectId, userId), "encrypted_project_secret")
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun offline_authenticationShouldThrowException() {

        val nonceScope = NonceScope(projectId, userId)

        val testObserver = ProjectAuthenticatorImpl(
            app.component,
            SafetyNet.getClient(app),
            createMockServiceToFailRequests(apiClient.retrofit))
            .authenticate(nonceScope, "encrypted_project_secret")
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(IOException::class.java)
    }

    @Test
    fun getAuthenticationData_invokeAuthenticationDataManagerCorrectly() {
        val authenticationDataManager = mockk<AuthenticationDataManager>(relaxed = true)
        val projectAuthenticator = ProjectAuthenticatorImpl(app.component, mockk(), mockk(), mockk(), authenticationDataManager)
        projectAuthenticator.getAuthenticationData(projectId, userId)

        verify(exactly = 1) { authenticationDataManager.requestAuthenticationData(projectId, userId) }
    }

    @Test
    fun authenticate_shouldAuthenticateWithRightRequestsToSecureApiInterface() {

        val mockWebServer = MockWebServer()
        val mockProjectSecretManager: ProjectSecretManager = mockk()

        every {
            mockProjectSecretManager.encryptAndStoreAndReturnProjectSecret(any(), any())
        } returns "encrypted_project_secret"

        mockWebServer.enqueue(mockResponseForAuthenticationData())
        mockWebServer.enqueue(mockResponseForApiToken())

        val authenticator = spyk(ProjectAuthenticatorImpl(
            app.component,
            SafetyNet.getClient(app),
            SimApiClientFactory("deviceId", mockWebServer.url("/").toString()).build<SecureApiInterface>().api,
            getMockAttestationManager()))

        every { authenticator.projectSecretManager } returns mockProjectSecretManager

        val testObserver = authenticator.authenticate(NonceScope(projectId, userId), "encrypted_project_secret").test()

        testObserver.awaitTerminalEvent()
        testObserver.assertNoErrors()
        Truth.assertThat(mockWebServer.requestCount).isEqualTo(2)
    }

    @Test
    fun safetyNetFailed_shouldThrowRightException() {
        val attestationManager = mockk<AttestationManager>()
        val nonceScope = NonceScope(projectId, userId)

        every {
            attestationManager.requestAttestation(any(), any())
        } throws (SafetyNetException(reason = SafetyNetExceptionReason.SERVICE_UNAVAILABLE))

        val testObserver = ProjectAuthenticatorImpl(
            app.component,
            SafetyNet.getClient(app),
            SecureApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
            attestationManager)
            .authenticate(nonceScope, projectId)
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(SafetyNetException::class.java)
    }

    private fun getMockAttestationManager(): AttestationManager {
        val attestationManager = mockk<AttestationManager>()
        every {
            attestationManager.requestAttestation(any(), any())
        } returns Single.just(AttestToken("google_attestation"))

        return attestationManager
    }

    private fun mockResponseForAuthenticationData() = MockResponse().let {
        it.setResponseCode(200)
        it.setBody(JsonHelper.toJson(ApiAuthenticationData("nonce", "publicKeyString")))
    }

    private fun mockResponseForApiToken() = MockResponse().let {
        it.setResponseCode(200)
        it.setBody(JsonHelper.toJson(ApiToken("firebaseCustomToken")))
    }
}
