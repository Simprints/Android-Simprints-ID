package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.infra.login.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.login.exceptions.SafetyNetException
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthenticationHelperImplTest {

    private lateinit var authenticationHelperImpl: AuthenticationHelperImpl
    private val loginInfoManager: LoginInfoManager = mockk(relaxed = true)
    private val timeHelper: TimeHelper = mockk(relaxed = true)
    private val projectAuthenticator: ProjectAuthenticator = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        authenticationHelperImpl =
            AuthenticationHelperImpl(
                loginInfoManager,
                timeHelper,
                projectAuthenticator,
                eventRepository
            )
    }

    @Test
    fun shouldSetBackendErrorIfBackendMaintenanceException() = runBlocking {
        val result = mockException(BackendMaintenanceException(estimatedOutage = 100))

        assertThat(result).isInstanceOf(Result.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun shouldSetOfflineIfIOException() = runBlocking {
        val result = mockException(IOException())

        assertThat(result).isInstanceOf(Result.OFFLINE::class.java)
    }

    @Test
    fun shouldSetSafetyNetUnavailableIfServiceUnavailableException() = runBlocking {
        val result =
            mockException(SafetyNetException(reason = SafetyNetException.SafetyNetExceptionReason.SERVICE_UNAVAILABLE))

        assertThat(result).isInstanceOf(Result.SAFETYNET_UNAVAILABLE::class.java)
    }

    @Test
    fun shouldSetSafetyNetInvalidIfSafetyNextInvalidException() = runBlocking {
        val result =
            mockException(SafetyNetException(reason = SafetyNetException.SafetyNetExceptionReason.INVALID_CLAIMS))

        assertThat(result).isInstanceOf(Result.SAFETYNET_INVALID_CLAIM::class.java)
    }

    @Test
    fun shouldSetUnknownIfGenericException() = runBlocking {
        val result = mockException(Exception())

        assertThat(result).isInstanceOf(Result.UNKNOWN::class.java)
    }

    @Test
    fun shouldTechnicalFailureIfSyncCloudIntegrationException() = runBlocking {
        val result = mockException(SyncCloudIntegrationException(cause = Exception()))

        assertThat(result).isInstanceOf(Result.TECHNICAL_FAILURE::class.java)
    }

    @Test
    fun shouldBadCredentialsIfAuthRequestInvalidCredentialsException() = runBlocking {
        val result = mockException(AuthRequestInvalidCredentialsException())

        assertThat(result).isInstanceOf(Result.BAD_CREDENTIALS::class.java)
    }

    private suspend fun mockException(exception: Exception): Result {
        coEvery { projectAuthenticator.authenticate(any(), "", "") } throws exception

        return authenticationHelperImpl.authenticateSafely("", "", "", "")
    }
}
