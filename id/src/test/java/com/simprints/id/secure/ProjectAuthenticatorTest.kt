package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.createMockBehaviorService
import com.simprints.id.tools.roboletric.TestApplication
import com.simprints.id.tools.roboletric.mockDbManager
import com.simprints.id.tools.roboletric.mockLocalDbManager
import com.simprints.id.tools.roboletric.mockRemoteDbManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class ProjectAuthenticatorTest : RxJavaTest() {

    private lateinit var app: Application
    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)
        mockRemoteDbManager(app)
        mockLocalDbManager(app)
        mockDbManager(app)
        apiClient = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl)
    }

    @Test
    fun successfulResponse_userShouldSignIn() {

        val authenticator = ProjectAuthenticator(
            mockSecureDataManager(),
            app.dataManager,
            SafetyNet.getClient(app),
            ApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
            getMockAttestationManager())

        val testObserver = authenticator
            .authenticate(NonceScope("project_id", "user_id"), "encrypted_project_secret")
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun offline_authenticationShouldThrowException() {

        val nonceScope = NonceScope("project_id", "user_id")

        val testObserver = ProjectAuthenticator(
            mockSecureDataManager(),
            app.dataManager,
            SafetyNet.getClient(app),
            createMockServiceToFailRequests(apiClient.retrofit))

            .authenticate(nonceScope, "encrypted_project_secret")
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(IOException::class.java)
    }
}
