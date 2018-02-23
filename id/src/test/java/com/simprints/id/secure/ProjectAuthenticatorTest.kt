package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.createMockService
import com.simprints.id.tools.retrofit.createMockServiceToFailRequests
import com.simprints.id.tools.roboletric.TestApplication
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

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)
        mockRemoteDbManager(app)
        mockLocalDbManager(app)
    }

    @Test
    fun successfulResponse_userShouldSignIn() {

        val authenticator = ProjectAuthenticator(
            SecureDataManagerMock(),
            app.dataManager,
            SafetyNet.getClient(app),
            createMockService(ApiService().retrofit, 0),
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
            SecureDataManagerMock(),
            app.dataManager,
            SafetyNet.getClient(app),
            createMockServiceToFailRequests(ApiService().retrofit))

            .authenticate(nonceScope, "encrypted_project_secret")
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(IOException::class.java)
    }
}
