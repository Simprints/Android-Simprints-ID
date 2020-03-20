package com.simprints.id.activities.login.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.tools.TimeHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class LoginRepositoryImplTest {

    @MockK lateinit var mockProjectAuthenticator: ProjectAuthenticator
    @MockK lateinit var mockSessionEventsManager: SessionEventsManager
    @MockK lateinit var mockTimeHelper: TimeHelper
    @MockK lateinit var mockCrashReportManager: CrashReportManager
    @MockK lateinit var mockLoginInfoManager: LoginInfoManager

    private lateinit var repository: LoginRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        val authenticationHelper = AuthenticationHelper(
            mockCrashReportManager,
            mockLoginInfoManager
        )

        repository = LoginRepositoryImpl(
            mockProjectAuthenticator,
            authenticationHelper,
            mockSessionEventsManager,
            mockTimeHelper
        )
    }

    @Test
    fun withCorrectCredentials_shouldAuthenticate() = runBlockingTest {
        coEvery { mockProjectAuthenticator.authenticate(any(), any()) } returns Unit

        val result = repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        assertThat(result).isEqualTo(AuthenticationEvent.Result.AUTHENTICATED)
    }

    @Test
    fun whenAuthenticatorThrowsIOException_shouldReturnOfflineResult() = runBlockingTest {
        coEvery { mockProjectAuthenticator.authenticate(any(), any()) } throws IOException()

        val result = repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        assertThat(result).isEqualTo(AuthenticationEvent.Result.OFFLINE)
    }

    @Test
    fun withInvalidCredentials_shouldReturnBadCredentialsResult() = runBlockingTest {
        coEvery {
            mockProjectAuthenticator.authenticate(any(), any())
        } throws AuthRequestInvalidCredentialsException()

        val result = repository.authenticate(
            "invalid_project_id",
            "invalid_user_id",
            "invalid_project_secret"
        )

        assertThat(result).isEqualTo(AuthenticationEvent.Result.BAD_CREDENTIALS)
    }

    @Test
    fun withCloudError_shouldReturnTechnicalFailureResult() = runBlockingTest {
        coEvery {
            mockProjectAuthenticator.authenticate(any(), any())
        } throws SimprintsInternalServerException()

        val result = repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        assertThat(result).isEqualTo(AuthenticationEvent.Result.TECHNICAL_FAILURE)
    }

    @Test
    fun whenSafetyNetIsDown_shouldReturnSafetyNetUnavailableResult() = runBlockingTest {
        coEvery {
            mockProjectAuthenticator.authenticate(any(), any())
        } throws SafetyNetException(reason = SafetyNetExceptionReason.SERVICE_UNAVAILABLE)

        val result = repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        assertThat(result).isEqualTo(AuthenticationEvent.Result.SAFETYNET_UNAVAILABLE)
    }

    @Test
    fun withInvalidSafetyNetClaims_shouldReturnSafetyNetInvalidClaimResult() = runBlockingTest {
        coEvery {
            mockProjectAuthenticator.authenticate(any(), any())
        } throws SafetyNetException(reason = SafetyNetExceptionReason.INVALID_CLAIMS)

        val result = repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        assertThat(result).isEqualTo(AuthenticationEvent.Result.SAFETYNET_INVALID_CLAIM)
    }

    @Test
    fun whenAuthenticatorThrowsRandomException_shouldReturnUnknownResult() = runBlockingTest {
        coEvery {
            mockProjectAuthenticator.authenticate(any(), any())
        } throws Throwable()

        val result = repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        assertThat(result).isEqualTo(AuthenticationEvent.Result.UNKNOWN)
    }

    @Test
    fun afterSuccessfulAuthentication_shouldAddSessionEvent() = runBlockingTest {
        coEvery { mockProjectAuthenticator.authenticate(any(), any()) } returns Unit

        repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        verify { mockSessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun afterUnsuccessfulAuthentication_shouldAddSessionEvent() = runBlockingTest {
        coEvery {
            mockProjectAuthenticator.authenticate(any(), any())
        } throws Throwable()

        repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        verify { mockSessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun withSuccessfulAuthentication_shouldLogToCrashReport() = runBlockingTest {
        coEvery { mockProjectAuthenticator.authenticate(any(), any()) } returns Unit

        repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        verify {
            mockCrashReportManager.logMessageForCrashReport(
                CrashReportTag.LOGIN,
                CrashReportTrigger.NETWORK,
                message = "Sign in success"
            )
        }
    }

    @Test
    fun withUnsuccessfulAuthentication_shouldLogToCrashReport() = runBlockingTest {
        coEvery { mockProjectAuthenticator.authenticate(any(), any()) } throws IOException()

        repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        verify {
            mockCrashReportManager.logMessageForCrashReport(
                CrashReportTag.LOGIN,
                CrashReportTrigger.NETWORK,
                message = "Sign in reason - OFFLINE"
            )
        }
    }

    @Test
    fun beforeAttemptingToAuthenticate_shouldLogToCrashReport() = runBlockingTest {
        repository.authenticate(
            "some_project_id",
            "some_user_id",
            "some_project_secret"
        )

        verify {
            mockCrashReportManager.logMessageForCrashReport(
                CrashReportTag.LOGIN,
                CrashReportTrigger.NETWORK,
                message = "Making authentication request"
            )
        }
    }

}
