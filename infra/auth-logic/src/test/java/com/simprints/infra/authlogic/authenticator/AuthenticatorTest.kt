package com.simprints.infra.authlogic.authenticator

import com.google.android.play.core.integrity.model.IntegrityErrorCode
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authlogic.model.AuthenticateDataResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.authlogic.integrity.exceptions.IntegrityServiceTemporaryDown
import com.simprints.infra.authlogic.integrity.exceptions.MissingOrOutdatedGooglePlayStoreApp
import com.simprints.infra.authlogic.integrity.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.events.EventRepository
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.IOException

internal class AuthenticatorTest {

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var projectAuthenticator: ProjectAuthenticator

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var authenticator: Authenticator


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        authenticator = Authenticator(
            authStore,
            timeHelper,
            projectAuthenticator,
            eventRepository,
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
    fun shouldSetMissingOrOutdatedGooglePlayStoreAppIfMissingOrOutdatedGooglePlayStoreAppException() = runBlocking {
        val result =
            mockException(MissingOrOutdatedGooglePlayStoreApp(IntegrityErrorCode.PLAY_STORE_VERSION_OUTDATED))
        assertThat(result).isInstanceOf(AuthenticateDataResult.MissingOrOutdatedGooglePlayStoreApp::class.java)
    }


    @Test
    fun shouldSetIntegrityServiceTemporaryDownIfIntegrityServiceTemporaryDown() = runBlocking {
        val result =
            mockException(IntegrityServiceTemporaryDown(IntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE))
        assertThat(result).isInstanceOf(AuthenticateDataResult.IntegrityServiceTemporaryDown::class.java)
    }

    @Test
    fun shouldSetIntegrityErrorIfServiceUnavailableException() = runBlocking {
        val result =
            mockException(RequestingIntegrityTokenException(IntegrityErrorCode.API_NOT_AVAILABLE))
        assertThat(result).isInstanceOf(AuthenticateDataResult.IntegrityException::class.java)
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

        return authenticator.authenticate("", "", "", "")
    }

    @Test
    fun `should return AUTHENTICATED if no exception`() = runBlocking {
        val result = authenticator.authenticate("", "", "", "")

        assertThat(result).isInstanceOf(AuthenticateDataResult.Authenticated::class.java)
    }
}
