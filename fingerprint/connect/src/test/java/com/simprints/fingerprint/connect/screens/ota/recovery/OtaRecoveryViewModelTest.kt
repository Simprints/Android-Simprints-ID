package com.simprints.fingerprint.connect.screens.ota.recovery

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class OtaRecoveryViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var scannerWrapper: ScannerWrapper

    @MockK
    private lateinit var scannerManager: ScannerManager

    private lateinit var viewModel: OtaRecoveryViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { scannerManager.scanner } returns scannerWrapper

        viewModel = OtaRecoveryViewModel(scannerManager)
    }

    @Test
    fun `test handleTryAgainPressed success`() {
        // Given
        coEvery { scannerWrapper.disconnect() } just Runs
        coEvery { scannerWrapper.connect() } just Runs
        // When
        viewModel.handleTryAgainPressed()
        val connectScannerStatus = viewModel.isConnectionSuccess.getOrAwaitValue()
        // Then
        assertThat(connectScannerStatus.peekContent()).isEqualTo(true)
    }

    @Test
    fun `test handleTryAgainPressed failure`() {
        // Given
        coEvery { scannerWrapper.disconnect() } throws IOException()
        coEvery { scannerWrapper.connect() } just Runs
        // When
        viewModel.handleTryAgainPressed()
        val connectScannerStatus = viewModel.isConnectionSuccess.getOrAwaitValue()
        // Then
        assertThat(connectScannerStatus.peekContent()).isEqualTo(false)
    }
}
