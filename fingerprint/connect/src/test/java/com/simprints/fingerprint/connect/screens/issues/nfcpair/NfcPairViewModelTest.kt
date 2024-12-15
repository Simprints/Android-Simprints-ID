package com.simprints.fingerprint.connect.screens.issues.nfcpair

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.fingerprint.infra.scanner.NfcManager
import com.simprints.fingerprint.infra.scanner.ScannerPairingManager
import com.simprints.infra.resources.R
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.assertEventReceivedWithContent
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class NfcPairViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var nfcManager: NfcManager

    @MockK
    private lateinit var scannerPairingManager: ScannerPairingManager

    private lateinit var viewModel: NfcPairViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = NfcPairViewModel(nfcManager, scannerPairingManager)
    }

    @Test
    fun `Starts pairing if valid mac address`() {
        every { nfcManager.readMacAddressDataFromBluetoothEasyPairTag(any()) } returns ADDRESS
        every { scannerPairingManager.isScannerAddress(any()) } returns true

        viewModel.handleNfcTagDetected(mockk())

        verify { scannerPairingManager.startPairingToDevice(ADDRESS) }
    }

    @Test
    fun `Shows error when not mac address`() {
        every { nfcManager.readMacAddressDataFromBluetoothEasyPairTag(any()) } returns ADDRESS
        every { scannerPairingManager.isScannerAddress(any()) } returns false

        val errorObserver = viewModel.showToastWithStringRes.testObserver()
        viewModel.handleNfcTagDetected(mockk())

        errorObserver.assertEventReceivedWithContent(R.string.fingerprint_connect_nfc_pair_toast_invalid)
        verify(exactly = 0) { scannerPairingManager.startPairingToDevice(ADDRESS) }
    }

    @Test
    fun `Shows error when failed to connect`() {
        every { nfcManager.readMacAddressDataFromBluetoothEasyPairTag(any()) } returns ADDRESS
        every { scannerPairingManager.isScannerAddress(any()) } returns true
        every { scannerPairingManager.startPairingToDevice(any()) } throws IOException("Test")

        val errorObserver = viewModel.showToastWithStringRes.testObserver()
        viewModel.handleNfcTagDetected(mockk())

        errorObserver.assertEventReceivedWithContent(R.string.fingerprint_connect_nfc_pair_toast_try_again)
    }

    @Test
    fun `Shows error when tag moved out of field`() {
        every { nfcManager.readMacAddressDataFromBluetoothEasyPairTag(any()) } returns ADDRESS
        every { scannerPairingManager.isScannerAddress(any()) } returns true
        every { scannerPairingManager.startPairingToDevice(any()) } throws SecurityException("Test")

        val errorObserver = viewModel.showToastWithStringRes.testObserver()
        viewModel.handleNfcTagDetected(mockk())

        errorObserver.assertEventReceivedWithContent(R.string.fingerprint_connect_nfc_pair_toast_try_again)
    }

    companion object {
        private const val ADDRESS = "address"
    }
}
