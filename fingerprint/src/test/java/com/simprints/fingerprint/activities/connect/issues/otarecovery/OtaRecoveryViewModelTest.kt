package com.simprints.fingerprint.activities.connect.issues.otarecovery

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class OtaRecoveryViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val scannerWrapper: ScannerWrapper = mockk()
    private val scannerManager: ScannerManager = mockk {
        every { scanner } returns scannerWrapper
    }
    private var viewModel = OtaRecoveryViewModel(scannerManager)

    @Test
    fun `test handleTryAgainPressed success`() {
        // Given
        coEvery { scannerWrapper.disconnect() } just Runs
        coEvery { scannerWrapper.connect() } just Runs
        //When
        viewModel.handleTryAgainPressed()
        val connectScannerStatus = viewModel.isConnectionSuccess.getOrAwaitValue()
        //Then
        Truth.assertThat(connectScannerStatus.peekContent()).isEqualTo(true)
    }

    @Test
    fun `test handleTryAgainPressed failure`() {
        // Given
        coEvery { scannerWrapper.disconnect() } throws IOException()
        coEvery { scannerWrapper.connect() } just Runs
        //When
        viewModel.handleTryAgainPressed()
        val connectScannerStatus = viewModel.isConnectionSuccess.getOrAwaitValue()
        //Then
        Truth.assertThat(connectScannerStatus.peekContent()).isEqualTo(false)
    }
}
