package com.simprints.fingerprint.activities.connect.issues.otarecovery

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FullUnitTestConfigRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import java.io.IOException

class OtaRecoveryViewModelTest : KoinTest {

    @get:Rule
    var unitTestConfigRule = FullUnitTestConfigRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: OtaRecoveryViewModel
    private val scannerWrapper: ScannerWrapper = mockk()
    private val scannerManager: ScannerManager = mockk {
        every { scanner } returns scannerWrapper
    }

    @Before
    fun setUp() {
        val mockModule = module {
            factory { scannerManager }
        }
        loadKoinModules(mockModule)

        viewModel = get()
    }

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
