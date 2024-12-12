package com.simprints.fingerprint.infra.scanner

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.data.FirmwareRepository
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.NullScannerException
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerFactory
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ScannerManagerImplTest {
    @MockK
    private lateinit var bluetoothAdapter: ComponentBluetoothAdapter

    @MockK
    private lateinit var scannerFactory: ScannerFactory

    @MockK
    private lateinit var pairingManager: ScannerPairingManager

    @MockK
    private lateinit var serialNumberConverter: SerialNumberConverter

    @MockK
    private lateinit var firmwareRepository: FirmwareRepository

    private lateinit var scannerManager: ScannerManagerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        scannerManager = ScannerManagerImpl(
            bluetoothAdapter = bluetoothAdapter,
            scannerFactory = scannerFactory,
            pairingManager = pairingManager,
            serialNumberConverter = serialNumberConverter,
            firmwareRepository = firmwareRepository,
        )
    }

    @Test
    fun `initScanner correctly initialises fields`() = runTest {
        coEvery { pairingManager.getPairedScannerAddressToUse() } returns "00:00:00:00:00:00"
        every { serialNumberConverter.convertMacAddressToSerialNumber(any()) } returns "000"
        every { scannerFactory.scannerWrapper } returns mockk()
        every { scannerFactory.scannerOtaOperationsWrapper } returns mockk()

        scannerManager.initScanner()

        assertThat(scannerManager.currentMacAddress).isEqualTo("00:00:00:00:00:00")
        assertThat(scannerManager.currentScannerId).isEqualTo("000")
        assertThat(scannerManager.scanner).isNotNull()
        assertThat(scannerManager.otaOperationsWrapper).isNotNull()
    }

    @Test
    fun `isScannerConnected returns false if not connected`() = runTest {
        coEvery { pairingManager.getPairedScannerAddressToUse() } returns "00:00:00:00:00:00"
        every { serialNumberConverter.convertMacAddressToSerialNumber(any()) } returns "000"
        every { scannerFactory.scannerOtaOperationsWrapper } returns mockk()
        every { scannerFactory.scannerWrapper } returns mockk {
            every { isConnected() } returns false
        }

        scannerManager.initScanner()
        assertThat(scannerManager.isScannerConnected).isFalse()
    }

    @Test
    fun `isScannerConnected returns true if connected`() = runTest {
        coEvery { pairingManager.getPairedScannerAddressToUse() } returns "00:00:00:00:00:00"
        every { serialNumberConverter.convertMacAddressToSerialNumber(any()) } returns "000"
        every { scannerFactory.scannerOtaOperationsWrapper } returns mockk()
        every { scannerFactory.scannerWrapper } returns mockk {
            every { isConnected() } returns true
        }

        scannerManager.initScanner()
        assertThat(scannerManager.isScannerConnected).isTrue()
    }

    @Test
    fun `isScannerConnected returns false if not connected initialised`() = runTest {
        assertThat(scannerManager.isScannerConnected).isFalse()
    }

    @Test
    fun `accessing scanner throws if not initialised`() = runTest {
        assertThrows<NullScannerException> { scannerManager.scanner }
    }

    @Test
    fun `accessing ota wrapper throws if not initialised`() = runTest {
        assertThrows<NullScannerException> { scannerManager.otaOperationsWrapper }
    }

    @Test
    fun `checkBluetoothStatus does not throw if BT enabled`() = runTest {
        every { bluetoothAdapter.isEnabled() } returns true
        scannerManager.checkBluetoothStatus()
    }

    @Test
    fun `checkBluetoothStatus throws exception BT disabled`() = runTest {
        every { bluetoothAdapter.isEnabled() } returns false

        assertThrows<BluetoothNotEnabledException> {
            scannerManager.checkBluetoothStatus()
        }
    }

    @Test
    fun deleteFirmwareFiles() = runTest {
        scannerManager.deleteFirmwareFiles()

        coVerify { firmwareRepository.deleteAllFirmwareFiles() }
    }
}
