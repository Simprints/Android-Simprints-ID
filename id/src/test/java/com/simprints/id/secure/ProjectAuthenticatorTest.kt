package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.createMockService
import com.simprints.id.tools.retrofit.createMockServiceToFailRequests
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class ProjectAuthenticatorTest : RxJavaTest() {

    @Test
    fun successfulResponse_canAuthenticateProjectNewCredentials() {

        val app = RuntimeEnvironment.application

        val authenticator = ProjectAuthenticator(SecureDataManagerMock(), createMockService(ApiService().retrofit, 0))
        authenticator.attestationManager = getMockAttestationManager()

        val testObserver = authenticator
            .authenticateWithNewCredentials(app, NonceScope("project_id", "user_id"), "encrypted_project_secret")
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun successfulResponse_canAuthenticateProjectExistingCredentials() {

        val app = RuntimeEnvironment.application

        val authenticator = ProjectAuthenticator(SecureDataManagerMock(), createMockService(ApiService().retrofit, 0))
        authenticator.attestationManager = getMockAttestationManager()

        val testObserver = authenticator
            .authenticateWithExistingCredentials(app, NonceScope("project_id", "user_id"))
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun offline_authenticationShouldThrowException() {

        val app = RuntimeEnvironment.application
        val nonceScope = NonceScope("project_id", "user_id")

        val testObserver = ProjectAuthenticator(SecureDataManagerMock(),
            createMockServiceToFailRequests(ApiService().retrofit))
            .authenticateWithExistingCredentials(app, nonceScope)
            .test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertError(IOException::class.java)
    }
}
