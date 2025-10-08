package com.simprints.feature.externalcredential.screens.scanqr

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.feature.externalcredential.screens.scanqr.usecase.ExternalCredentialQrCodeValidatorUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ExternalCredentialScanQrViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var validator: ExternalCredentialQrCodeValidatorUseCase

    private lateinit var viewModel: ExternalCredentialScanQrViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = ExternalCredentialScanQrViewModel(validator)
    }

    @Test
    fun `initial state is ReadyToScan`() {
        val observer = viewModel.stateLiveData.test()
        assertThat(observer.value()).isEqualTo(ScanQrState.ReadyToScan)
    }

    @Test
    fun `updateCapturedValue with null sets ReadyToScan`() {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCapturedValue(null)
        assertThat(observer.value()).isEqualTo(ScanQrState.ReadyToScan)
    }

    @Test
    fun `updateCapturedValue with non-null sets QrCodeCaptured`() {
        val observer = viewModel.stateLiveData.test()
        viewModel.updateCapturedValue("captured-value")
        assertThat(observer.value()).isEqualTo(ScanQrState.QrCodeCaptured("captured-value"))
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
        val validValue = "validValue"
        val invalidValue = "invalidValue"
        every { validator.invoke(validValue) } returns true
        every { validator.invoke(invalidValue) } returns false

        assertThat(viewModel.isValidQrCodeFormat(validValue)).isTrue()
        assertThat(viewModel.isValidQrCodeFormat(invalidValue)).isFalse()
    }
}
