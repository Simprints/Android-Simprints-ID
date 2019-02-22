package com.simprints.id.secure

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.safetynet.SafetyNet
import com.simprints.id.commontesttools.di.DependencyRule.MockRule
import com.simprints.id.commontesttools.di.DependencyRule.ReplaceRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker
import com.simprints.id.testtools.state.mockLoginInfoManager
import com.simprints.id.testtools.state.setupFakeKeyStore
import com.simprints.testframework.common.retrofit.createMockBehaviorService
import com.simprints.testframework.common.syntax.anyNotNull
import com.simprints.testframework.common.syntax.mock
import com.simprints.testframework.common.syntax.verifyOnce
import com.simprints.testframework.common.syntax.whenever
import com.simprints.testframework.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testframework.unit.robolectric.getSharedPreferences
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ProjectAuthenticatorTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var remoteProjectManagerMock: RemoteProjectManager
    @Inject lateinit var remoteSessionsManagerMock: RemoteSessionsManager
    @Inject lateinit var loginInfoManagerMock: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var longConsentManager: LongConsentManager
    @Inject lateinit var peopleUpSyncMasterMock: PeopleUpSyncMaster

    private val projectId = "project_id"
    private val userId = "user_id"

    private val module by lazy {
        TestAppModule(
            app,
            localDbManagerRule = MockRule,
            remoteDbManagerRule = MockRule,
            remoteProjectManagerRule = MockRule,
            loginInfoManagerRule = MockRule,
            syncSchedulerHelperRule = MockRule,
            longConsentManagerRule = MockRule,
            peopleUpSyncMasterRule = MockRule,
            keystoreManagerRule = ReplaceRule { setupFakeKeyStore() }
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        RobolectricTestMocker
            .initLogInStateMock(getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME), remoteDbManagerMock)
            .mockLoadProject(localDbManagerMock, remoteProjectManagerMock)

        mockLoginInfoManager(loginInfoManagerMock)
        whenever(remoteSessionsManagerMock.getSessionsApiClient()).thenReturn(Single.create { it.onError(IllegalStateException()) })
        whenever(longConsentManager.downloadAllLongConsents(anyNotNull())).thenReturn(Completable.complete())

        apiClient = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl)
    }

    @Test
    fun successfulResponse_userShouldSignIn() {

        val authenticator = LegacyCompatibleProjectAuthenticator(
            app.component,
            SafetyNet.getClient(app),
            SecureApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
            getMockAttestationManager())

        val testObserver = authenticator
            .authenticate(NonceScope(projectId, userId), "encrypted_project_secret", projectId, null)
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()

        verifyOnce(peopleUpSyncMasterMock) { resume(projectId/*, userId*/) } // TODO: uncomment userId when multitenancy is properly implemented
    }

    @Test
    fun offline_authenticationShouldThrowException() {

        val nonceScope = NonceScope(projectId, userId)

        val testObserver = LegacyCompatibleProjectAuthenticator(
            app.component,
            SafetyNet.getClient(app),
            createMockServiceToFailRequests(apiClient.retrofit))
            .authenticate(nonceScope, "encrypted_project_secret", projectId, null)
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(IOException::class.java)
    }

    private fun getMockAttestationManager(): AttestationManager {
        val attestationManager = mock<AttestationManager>()
        whenever(attestationManager.requestAttestation(anyNotNull(), anyNotNull())).thenReturn(Single.just(AttestToken("google_attestation")) )
        return attestationManager
    }
}
