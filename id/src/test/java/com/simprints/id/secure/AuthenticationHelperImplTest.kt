package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.id.secure.models.AuthenticateDataResult
import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.login.exceptions.SafetyNetException
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthenticationHelperImplTest {

    private lateinit var authenticationHelperImpl: AuthenticationHelperImpl
    private val loginManager: LoginManager = mockk(relaxed = true)
    private val timeHelper: TimeHelper = mockk(relaxed = true)
    private val projectAuthenticator: ProjectAuthenticator = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        authenticationHelperImpl =
            AuthenticationHelperImpl(
                loginManager,
                timeHelper,
                projectAuthenticator,
                eventRepository
            )
    }

    @Test
    fun shouldSetBackendErrorIfBackendMaintenanceExceptionWithoutTime() = runBlocking {
        val result = mockException(BackendMaintenanceException(estimatedOutage = null))

        assertThat(result).isInstanceOf(AuthenticateDataResult.BackendMaintenanceError::class.java)
    }

    @Test
    fun shouldSetOfflineIfIOException() = runBlocking {
        val result = mockException(IOException())

        assertThat(result).isInstanceOf(AuthenticateDataResult.Offline::class.java)
    }

    @Test
    fun shouldSetOfflineIfNetworkConnectionException() = runBlocking {
        val result = mockException(
            NetworkConnectionException(
                cause = Throwable()
            )
        )

        assertThat(result).isInstanceOf(AuthenticateDataResult.Offline::class.java)
    }

    @Test
    fun shouldSetSafetyNetUnavailableIfServiceUnavailableException() = runBlocking {
        val result =
            mockException(SafetyNetException(reason = SafetyNetException.SafetyNetExceptionReason.SERVICE_UNAVAILABLE))

        assertThat(result).isInstanceOf(AuthenticateDataResult.SafetyNetUnavailable::class.java)
    }

    @Test
    fun shouldSetSafetyNetInvalidIfSafetyNextInvalidException() = runBlocking {
        val result =
            mockException(SafetyNetException(reason = SafetyNetException.SafetyNetExceptionReason.INVALID_CLAIMS))

        assertThat(result).isInstanceOf(AuthenticateDataResult.SafetyNetInvalidClaim::class.java)
    }

    @Test
    fun shouldSetUnknownIfGenericException() = runBlocking {
        val result = mockException(Exception())

        assertThat(result).isInstanceOf(AuthenticateDataResult.Unknown::class.java)
    }

    @Test
    fun shouldTechnicalFailureIfSyncCloudIntegrationException() = runBlocking {
        val result = mockException(SyncCloudIntegrationException(cause = Exception()))

        assertThat(result).isInstanceOf(AuthenticateDataResult.TechnicalFailure::class.java)
    }

    @Test
    fun shouldBadCredentialsIfAuthRequestInvalidCredentialsException() = runBlocking {
        val result = mockException(AuthRequestInvalidCredentialsException())

        assertThat(result).isInstanceOf(AuthenticateDataResult.BadCredentials::class.java)
    }

    private suspend fun mockException(exception: Exception): AuthenticateDataResult {
        coEvery { projectAuthenticator.authenticate(any(), "", "") } throws exception

        return authenticationHelperImpl.authenticateSafely("", "", "", "")
    }

    @Test
    fun `should return AUTHENTICATED if no exception`() = runBlocking {
        val result = authenticationHelperImpl.authenticateSafely("", "", "", "")

        assertThat(result).isInstanceOf(AuthenticateDataResult.Authenticated::class.java)
    }
}
