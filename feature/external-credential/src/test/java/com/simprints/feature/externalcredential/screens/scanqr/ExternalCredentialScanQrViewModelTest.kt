package com.simprints.feature.externalcredential.screens.scanqr

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.externalcredential.screens.scanqr.usecase.ExternalCredentialQrCodeValidatorUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ExternalCredentialScanQrViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var validator: ExternalCredentialQrCodeValidatorUseCase

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configManager: ConfigManager

    private lateinit var viewModel: ExternalCredentialScanQrViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = ExternalCredentialScanQrViewModel(
            externalCredentialQrCodeValidator = validator,
            tokenizationProcessor = tokenizationProcessor,
            configManager = configManager,
            authStore = authStore,
        )
    }

    @Test
    fun `initial state is ReadyToScan`() {
        val observer = viewModel.stateLiveData.test()
        assertThat(observer.value()).isEqualTo(ScanQrState.ReadyToScan)
    }

    @Test
    fun `updateCapturedValue with null sets ReadyToScan`() = runTest {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCapturedValue(null)
        assertThat(observer.value()).isEqualTo(ScanQrState.ReadyToScan)
    }

    @Test
    fun `updateCapturedValue with non-null sets QrCodeCaptured`() = runTest {
        val observer = viewModel.stateLiveData.test()
        val value = "value"
        val projectId = "projectId"
        val mockProject = mockk<Project>()
        val mockTokenizedCredential = mockk<TokenizableString.Tokenized>()

        every { authStore.signedInProjectId } returns projectId
        coEvery { configManager.getProject(projectId) } returns mockProject
        coEvery { tokenizationProcessor.encrypt(any(), TokenKeyType.ExternalCredential, mockProject) } returns mockTokenizedCredential

        viewModel.updateCapturedValue(value)

        val expected = ScanQrState.QrCodeCaptured(value.asTokenizableRaw(), mockTokenizedCredential)
        assertThat(observer.value()).isEqualTo(expected)
    }

    @Test
    fun `updateCameraPermissionStatus with Granted sets ReadyToScan`() {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCameraPermissionStatus(PermissionStatus.Granted)
        assertThat(observer.value()).isEqualTo(ScanQrState.ReadyToScan)
    }

    @Test
    fun `updateCameraPermissionStatus with Denied sets NoCameraPermission false`() {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCameraPermissionStatus(PermissionStatus.Denied)
        assertThat(observer.value()).isEqualTo(
            ScanQrState.NoCameraPermission(shouldOpenPhoneSettings = false),
        )
    }

    @Test
    fun `updateCameraPermissionStatus with DeniedNeverAskAgain sets NoCameraPermission true`() {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCameraPermissionStatus(PermissionStatus.DeniedNeverAskAgain)
        assertThat(observer.value()).isEqualTo(
            ScanQrState.NoCameraPermission(shouldOpenPhoneSettings = true),
        )
    }

    @Test
    fun `isValidQrCodeFormat uses validator to validate`() {
        val validValue = "validValue".asTokenizableRaw()
        val invalidValue = "invalidValue".asTokenizableRaw()
        every { validator.invoke(validValue.value) } returns true
        every { validator.invoke(invalidValue.value) } returns false

        assertThat(viewModel.isValidQrCodeFormat(validValue)).isTrue()
        assertThat(viewModel.isValidQrCodeFormat(invalidValue)).isFalse()
    }
}
