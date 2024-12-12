package com.simprints.feature.login.screens.form

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.screens.qrscanner.QrCodeContent
import com.simprints.feature.login.screens.qrscanner.QrScannerResult
import com.simprints.feature.login.screens.qrscanner.QrScannerResult.QrScannerError
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authlogic.model.AuthenticateDataResult
import com.simprints.infra.network.SimNetwork
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

internal class LoginFormViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var simNetwork: SimNetwork

    @MockK
    private lateinit var authManager: AuthManager

    @MockK
    private lateinit var jsonHelper: JsonHelper

    private lateinit var viewModel: LoginFormViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = LoginFormViewModel(
            DEVICE_ID,
            simNetwork,
            authManager,
            jsonHelper,
        )
    }

    @Test
    fun `returns MissingCredentials when empty user id`() {
        viewModel.signInClicked(LoginParams(PROJECT_ID, "".asTokenizableRaw()), PROJECT_ID, PROJECT_SECRET)

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.MissingCredential::class.java)
    }

    @Test
    fun `returns MissingCredentials when empty project id`() {
        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), "", PROJECT_SECRET)

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.MissingCredential::class.java)
    }

    @Test
    fun `returns MissingCredentials when empty project secret`() {
        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), PROJECT_ID, "")

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.MissingCredential::class.java)
    }

    @Test
    fun `returns ProjectIdMismatch when login and actual project ids not match`() {
        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), "otherProjectId", PROJECT_SECRET)

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.ProjectIdMismatch::class.java)
    }

    @Test
    fun `returns false from isProcessingSignIn sign in request is processed`() {
        coEvery { authManager.authenticateSafely(any(), any(), any(), any()) } returns AuthenticateDataResult.Authenticated
        val observer = mockk<Observer<Boolean>>()
        val slot = slot<Boolean>()
        val capturedValues = mutableListOf<Boolean>()
        every { observer.onChanged(capture(slot)) } answers {
            capturedValues.add(slot.captured)
        }
        viewModel.isProcessingSignIn.observeForever(observer)

        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), PROJECT_ID, PROJECT_SECRET)

        // Checking that captured values sequence contains 'true' and 'false', in that order
        assertThat(capturedValues).isEqualTo(listOf(true, false))
    }

    @Test
    fun `returns correct SignInState for each auth result class`() {
        mapOf(
            AuthenticateDataResult.Authenticated to SignInState.Success::class.java,
            AuthenticateDataResult.BadCredentials to SignInState.BadCredentials::class.java,
            AuthenticateDataResult.IntegrityException to SignInState.IntegrityException::class.java,
            AuthenticateDataResult.IntegrityServiceTemporaryDown to SignInState.IntegrityServiceTemporaryDown::class.java,
            AuthenticateDataResult.MissingOrOutdatedGooglePlayStoreApp to SignInState.MissingOrOutdatedGooglePlayStoreApp::class.java,
            AuthenticateDataResult.Offline to SignInState.Offline::class.java,
            AuthenticateDataResult.TechnicalFailure to SignInState.TechnicalFailure::class.java,
            AuthenticateDataResult.Unknown to SignInState.Unknown::class.java,
            AuthenticateDataResult.BackendMaintenanceError() to SignInState.BackendMaintenanceError::class.java,
        ).forEach { (provided, expected) ->
            clearMocks(authManager)
            coEvery { authManager.authenticateSafely(any(), any(), any(), any()) } returns provided
            viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), PROJECT_ID, PROJECT_SECRET)

            val result = viewModel.signInState.getOrAwaitValue()

            assertThat(result.getContentIfNotHandled()).isInstanceOf(expected)
        }
    }

    @Test
    fun `returns correct SignInState with estimated outage`() {
        coEvery { authManager.authenticateSafely(any(), any(), any(), any()) } returns
            AuthenticateDataResult.BackendMaintenanceError(100)

        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), PROJECT_ID, PROJECT_SECRET)

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat((result.getContentIfNotHandled() as SignInState.BackendMaintenanceError).estimatedOutage)
            .isEqualTo("01 minutes, 40 seconds")
    }

    @Test
    fun `returns correct SignInState on QR error`() {
        mapOf(
            QrScannerError.NoPermission to SignInState.QrNoCameraPermission::class.java,
            QrScannerError.CameraNotAvailable to SignInState.QrCameraUnavailable::class.java,
            QrScannerError.UnknownError to SignInState.QrGenericError::class.java,
        ).forEach { (error, expected) ->
            viewModel.handleQrResult(PROJECT_ID, QrScannerResult(null, error))
            val result = viewModel.signInState.getOrAwaitValue()

            assertThat(result.getContentIfNotHandled()).isInstanceOf(expected)
        }
    }

    @Test
    fun `returns correct SignInState when empty QR result`() {
        viewModel.handleQrResult(PROJECT_ID, QrScannerResult(null, null))
        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.QrInvalidCode::class.java)
    }

    @Test
    fun `returns correct SignInState when QR code parsing fails`() {
        every { jsonHelper.fromJson<QrCodeContent>(any()) } throws RuntimeException("parsing fail")

        viewModel.handleQrResult(PROJECT_ID, QrScannerResult(QR_CONTENT, null))
        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.QrInvalidCode::class.java)
    }

    @Test
    fun `returns correct SignInState when QR contains wrong project ID`() {
        every { jsonHelper.fromJson<QrCodeContent>(eq(QR_CONTENT)) } returns QrCodeContent("differentProjectId", PROJECT_SECRET)

        viewModel.handleQrResult(PROJECT_ID, QrScannerResult(QR_CONTENT, null))
        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.ProjectIdMismatch::class.java)
    }

    @Test
    fun `returns correct SignInState when QR code parsing success`() {
        every { jsonHelper.fromJson<QrCodeContent>(eq(QR_CONTENT)) } returns QrCodeContent(PROJECT_ID, PROJECT_SECRET)

        viewModel.handleQrResult(PROJECT_ID, QrScannerResult(QR_CONTENT, null))
        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.QrCodeValid::class.java)
        assertThat((result.peekContent() as SignInState.QrCodeValid).projectId).isEqualTo(PROJECT_ID)
        assertThat((result.peekContent() as SignInState.QrCodeValid).projectSecret).isEqualTo(PROJECT_SECRET)
    }

    @Test
    fun `updates base API url when QR code parsing success`() {
        every { jsonHelper.fromJson<QrCodeContent>(eq(QR_CONTENT)) } returns QrCodeContent(PROJECT_ID, PROJECT_SECRET, URL)

        viewModel.handleQrResult(PROJECT_ID, QrScannerResult(QR_CONTENT, null))

        verify { simNetwork.setApiBaseUrl(eq(URL)) }
    }

    @Test
    fun `updates UI state when change URL clicked`() {
        viewModel.changeUrlClicked()

        val result = viewModel.signInState.getOrAwaitValue()
        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.ShowUrlChangeDialog::class.java)
    }

    @Test
    fun `saves provided base URL`() {
        viewModel.saveNewUrl(URL)

        verify { simNetwork.setApiBaseUrl(URL) }
    }

    @Test
    fun `resets provided base URL`() {
        viewModel.saveNewUrl(null)

        verify { simNetwork.resetApiBaseUrl() }
    }

    companion object {
        private const val DEVICE_ID = "deviceId"
        private const val PROJECT_ID = "projectId"
        private val USER_ID = "userId".asTokenizableRaw()

        private const val QR_CONTENT = "qrCodeContents"
        private const val PROJECT_SECRET = "projectSecret"
        private const val URL = "projectUrl"
    }
}
