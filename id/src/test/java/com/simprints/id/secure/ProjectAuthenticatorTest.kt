package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.createMockService
import com.simprints.id.tools.retrofit.createMockServiceToFailRequests
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
@Config(constants = BuildConfig::class)
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

        val authenticator = ProjectAuthenticator(SecureDataManagerMock(), app.dataManager, createMockService(ApiService().retrofit, 0))
        authenticator.attestationManager = getMockAttestationManager()

        val testObserver = authenticator
            .authenticateWithNewCredentials(SafetyNet.getClient(app), NonceScope("project_id", "user_id"), "encrypted_project_secret")
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
            createMockServiceToFailRequests(ApiService().retrofit))
            .authenticateWithNewCredentials(SafetyNet.getClient(app), nonceScope, "encrypted_project_secret")
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(IOException::class.java)
    }
}
