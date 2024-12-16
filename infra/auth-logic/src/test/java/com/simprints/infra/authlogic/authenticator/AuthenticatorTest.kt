package com.simprints.infra.authlogic.authenticator

import com.google.android.play.core.integrity.model.IntegrityErrorCode
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authlogic.integrity.exceptions.IntegrityServiceTemporaryDown
import com.simprints.infra.authlogic.integrity.exceptions.MissingOrOutdatedGooglePlayStoreApp
import com.simprints.infra.authlogic.integrity.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.authlogic.model.AuthenticateDataResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
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
    private lateinit var eventRepository: SessionEventRepository

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
    fun shouldSetBackendErrorIfBackendMaintenanceExceptionWithoutTime() = runTest {
        val result = mockException(BackendMaintenanceException(estimatedOutage = null))

        assertThat(result).isInstanceOf(AuthenticateDataResult.BackendMaintenanceError::class.java)
    }

    @Test
    fun shouldSetOfflineIfIOException() = runTest {
        val result = mockException(IOException())

        assertThat(result).isInstanceOf(AuthenticateDataResult.Offline::class.java)
    }

    @Test
    fun shouldSetOfflineIfNetworkConnectionException() = runTest {
        val result = mockException(
            NetworkConnectionException(
                cause = Throwable(),
            ),
        )

        assertThat(result).isInstanceOf(AuthenticateDataResult.Offline::class.java)
    }

    @Test
    fun shouldSetMissingOrOutdatedGooglePlayStoreAppIfMissingOrOutdatedGooglePlayStoreAppException() = runTest {
        val result =
            mockException(MissingOrOutdatedGooglePlayStoreApp(IntegrityErrorCode.PLAY_STORE_VERSION_OUTDATED))
        assertThat(result).isInstanceOf(AuthenticateDataResult.MissingOrOutdatedGooglePlayStoreApp::class.java)
    }

    @Test
    fun shouldSetIntegrityServiceTemporaryDownIfIntegrityServiceTemporaryDown() = runTest {
        val result =
            mockException(IntegrityServiceTemporaryDown(IntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE))
        assertThat(result).isInstanceOf(AuthenticateDataResult.IntegrityServiceTemporaryDown::class.java)
    }

    @Test
    fun shouldSetIntegrityErrorIfServiceUnavailableException() = runTest {
        val result =
            mockException(RequestingIntegrityTokenException(IntegrityErrorCode.API_NOT_AVAILABLE))
        assertThat(result).isInstanceOf(AuthenticateDataResult.IntegrityException::class.java)
    }

    @Test
    fun shouldSetUnknownIfGenericException() = runTest {
        val result = mockException(Exception())

        assertThat(result).isInstanceOf(AuthenticateDataResult.Unknown::class.java)
    }

    @Test
    fun shouldTechnicalFailureIfSyncCloudIntegrationException() = runTest {
        val result = mockException(SyncCloudIntegrationException(cause = Exception()))

        assertThat(result).isInstanceOf(AuthenticateDataResult.TechnicalFailure::class.java)
    }

    @Test
    fun shouldBadCredentialsIfAuthRequestInvalidCredentialsException() = runTest {
        val result = mockException(AuthRequestInvalidCredentialsException())

        assertThat(result).isInstanceOf(AuthenticateDataResult.BadCredentials::class.java)
    }

    private suspend fun mockException(exception: Exception): AuthenticateDataResult {
        coEvery { projectAuthenticator.authenticate(any(), "") } throws exception

        return authenticator.authenticate(
            userId = "".asTokenizableRaw(),
            projectId = "",
            projectSecret = "",
            deviceId = "",
        )
    }

    @Test
    fun `should return AUTHENTICATED if no exception`() = runTest {
        val result = authenticator.authenticate(
            userId = "".asTokenizableRaw(),
            projectId = "",
            projectSecret = "",
            deviceId = "",
        )

        assertThat(result).isInstanceOf(AuthenticateDataResult.Authenticated::class.java)
    }
}
