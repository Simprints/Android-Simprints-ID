package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.verify
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.createMockBehaviorService
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.getRoboSharedPreferences
import com.simprints.id.testUtils.roboletric.initLogInStateMock
import com.simprints.id.testUtils.roboletric.mockLoadProject
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class ProjectAuthenticatorTest : RxJavaTest, DaggerForTests() {

    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var loginInfoManagerMock: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var longConsentManager: LongConsentManager
    @Inject lateinit var peopleUpSyncMasterMock: PeopleUpSyncMaster

    private val projectId = "project_id"
    private val userId = "user_id"

    override var module by lazyVar {
        AppModuleForTests(
            app,
            localDbManagerRule = MockRule,
            remoteDbManagerRule = MockRule,
            loginInfoManagerRule = MockRule,
            scheduledPeopleSyncManagerRule = MockRule,
            longConsentManagerRule = MockRule,
            peopleUpSyncMasterRule = MockRule
        )
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)

        initLogInStateMock(getRoboSharedPreferences(), remoteDbManagerMock)

        mockLoadProject(localDbManagerMock, remoteDbManagerMock)
        mockLoginInfoManager(loginInfoManagerMock)
        whenever(remoteDbManagerMock.getSessionsApiClient()).thenReturn(Single.create { it.onError(IllegalStateException()) })
        whenever(longConsentManager.downloadAllLongConsents(anyNotNull())).thenReturn(Completable.complete())

        apiClient = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl)
    }

    @Test
    fun successfulResponse_userShouldSignIn() {

        val authenticator = LegacyCompatibleProjectAuthenticator(
            testAppComponent,
            SafetyNet.getClient(app),
            ApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
            getMockAttestationManager())

        val testObserver = authenticator
            .authenticate(NonceScope(projectId, userId), "encrypted_project_secret", projectId, null)
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()

        verify(peopleUpSyncMasterMock).resume(projectId, userId)
    }

    @Test
    fun offline_authenticationShouldThrowException() {

        val nonceScope = NonceScope(projectId, userId)

        val testObserver = LegacyCompatibleProjectAuthenticator(
            testAppComponent,
            SafetyNet.getClient(app),
            createMockServiceToFailRequests(apiClient.retrofit))
            .authenticate(nonceScope, "encrypted_project_secret", projectId, null)
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(IOException::class.java)
    }
}
