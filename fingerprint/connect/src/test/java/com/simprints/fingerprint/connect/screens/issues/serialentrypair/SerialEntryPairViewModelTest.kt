package com.simprints.fingerprint.connect.screens.issues.serialentrypair

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.ScannerPairingManager
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class SerialEntryPairViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var scannerPairViewModel: ScannerPairingManager

    @MockK
    lateinit var serialNumberConverter: SerialNumberConverter

    lateinit var viewModel: SerialEntryPairViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = SerialEntryPairViewModel(
            scannerPairViewModel,
            serialNumberConverter,
        )
    }

    @Test
    fun `Starts pairing when serial received`() {
        every { serialNumberConverter.convertSerialNumberToMacAddress(any()) } returns ADDRESS

        viewModel.startPairing(ADDRESS)

        verify { scannerPairViewModel.startPairingToDevice(ADDRESS) }
    }

    @Test
    fun `when serial number is set, then its value is stored correctly`() {
        assertThat(viewModel.scannerNumber).isNull()
        val expected = "12345"
        viewModel.scannerNumber = expected
        assertThat(viewModel.scannerNumber).isEqualTo(expected)
    }

    companion object {
        private const val ADDRESS = "address"
    }
}
