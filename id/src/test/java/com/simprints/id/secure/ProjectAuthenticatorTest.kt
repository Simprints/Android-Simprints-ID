package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseApp
import com.simprints.id.DaggerTest
import com.simprints.id.TestAppModule
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.retrofit.createMockBehaviorService
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.getRoboSharedPreferences
import com.simprints.id.testUtils.roboletric.initLogInStateMock
import com.simprints.id.testUtils.roboletric.mockLoadProject
import com.simprints.id.tools.delegates.lazyVar
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
class ProjectAuthenticatorTest : RxJavaTest, DaggerTest() {

    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var loginInfoManagerMock: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var secureDataManager: SecureDataManager

    override var module by lazyVar {
        TestAppModule(app, localDbManagerSpy = false, remoteDbManagerSpy = false, loginInfoManagerSpy = false)
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

        apiClient = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl)
    }

    @Test
    fun successfulResponse_userShouldSignIn() {

        val authenticator = LegacyCompatibleProjectAuthenticator(
            loginInfoManagerMock,
            dbManager,
            secureDataManager,
            SafetyNet.getClient(app),
            ApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
            getMockAttestationManager())

        val testObserver = authenticator
            .authenticate(NonceScope("project_id", "user_id"), "encrypted_project_secret", "project_id", null)
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun offline_authenticationShouldThrowException() {

        val nonceScope = NonceScope("project_id", "user_id")

        val testObserver = LegacyCompatibleProjectAuthenticator(
            loginInfoManagerMock,
            dataManager,
            secureDataManager,
            SafetyNet.getClient(app),
            createMockServiceToFailRequests(apiClient.retrofit))
            .authenticate(nonceScope, "encrypted_project_secret", "project_id", null)
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(IOException::class.java)
    }
}
