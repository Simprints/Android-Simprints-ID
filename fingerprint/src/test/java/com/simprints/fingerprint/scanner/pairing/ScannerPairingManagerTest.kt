package com.simprints.fingerprint.scanner.pairing

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscannermock.dummy.DummyBluetoothDevice
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ScannerPairingManagerTest {

    private val bluetoothAdapterMock = mockk<ComponentBluetoothAdapter>()
    private val scannerGenerationDeterminerMock = mockk<ScannerGenerationDeterminer> {
        every { determineScannerGenerationFromSerialNumber(eq(correctSerial)) } returns correctGeneration
        every { determineScannerGenerationFromSerialNumber(eq(incorrectSerial)) } returns incorrectGeneration
    }
    private val serialNumberConverterMock = mockk<SerialNumberConverter> {
        every { convertMacAddressToSerialNumber(eq(correctAddress)) } returns correctSerial
        every { convertMacAddressToSerialNumber(eq(incorrectAddress)) } returns incorrectSerial
        every { convertMacAddressToSerialNumber(eq(someOtherAddress)) } returns someOtherSerial
        every { convertSerialNumberToMacAddress(eq(correctSerial)) } returns correctAddress
        every { convertSerialNumberToMacAddress(eq(incorrectSerial)) } returns incorrectAddress
        every { convertSerialNumberToMacAddress(eq(someOtherSerial)) } returns someOtherAddress
    }
    private val recentEventsPreferencesManager = mockk<FingerprintPreferencesManager>()
    private val fingerprintConfiguration = mockk<FingerprintConfiguration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
    }
    private val scannerPairingManager = ScannerPairingManager(
        bluetoothAdapterMock,
        recentEventsPreferencesManager,
        scannerGenerationDeterminerMock,
        serialNumberConverterMock,
        configManager
    )

    @Test
    fun interpretEnteredTextAsSerialNumber_worksCorrectlyForValidStrings() {
        assertThat(scannerPairingManager.interpretEnteredTextAsSerialNumber("987654")).isEqualTo("SP987654")
        assertThat(scannerPairingManager.interpretEnteredTextAsSerialNumber("012345")).isEqualTo("SP012345")
    }

    @Test
    fun interpretEnteredTextAsSerialNumber_throwsForInvalidStrings() {
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "12345"
            )
        }
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "9876543"
            )
        }
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "-12345"
            )
        }
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "1234o2"
            )
        }
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "1874.5"
            )
        }
    }

    @Test
    fun getPairedScannerAddressToUse_oneValidPairedDevice_returnsCorrectly() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = correctAddress))
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(correctGeneration)

        assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(correctAddress)
    }

    @Test
    fun getPairedScannerAddressToUse_oneInvalidPairedDevice_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = incorrectAddress))
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(correctGeneration)

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_noScannersPaired_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = notScannerAddress))

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_noDevicesPaired_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf()

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_multipleDevicesWithOneValidScanner_returnsCorrectly() =
        runTest {
            every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
                DummyBluetoothDevice(address = correctAddress),
                DummyBluetoothDevice(address = incorrectAddress),
                DummyBluetoothDevice(address = notScannerAddress)
            )
            every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(
                correctGeneration
            )

            assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(
                correctAddress
            )
        }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_lastScannerUsedExistsAndIsPaired() =
        runTest {
            every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
                DummyBluetoothDevice(address = correctAddress),
                DummyBluetoothDevice(address = incorrectAddress),
                DummyBluetoothDevice(address = notScannerAddress)
            )
            every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(
                correctGeneration,
                incorrectGeneration
            )
            every { recentEventsPreferencesManager.lastScannerUsed } returns correctSerial

            assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(
                correctAddress
            )
        }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_noLastScannerUsed_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = correctAddress),
            DummyBluetoothDevice(address = incorrectAddress),
            DummyBluetoothDevice(address = notScannerAddress)
        )
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(
            correctGeneration,
            incorrectGeneration
        )
        every { recentEventsPreferencesManager.lastScannerUsed } returns ""

        assertThrows<MultiplePossibleScannersPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_lastScannerUsedNotPaired_throws() =
        runTest {
            every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
                DummyBluetoothDevice(address = correctAddress),
                DummyBluetoothDevice(address = incorrectAddress),
                DummyBluetoothDevice(address = notScannerAddress)
            )
            every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(
                correctGeneration,
                incorrectGeneration
            )
            every { recentEventsPreferencesManager.lastScannerUsed } returns someOtherSerial

            assertThrows<MultiplePossibleScannersPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
        }

    companion object {
        private const val correctAddress = "F0:AC:D7:C0:00:01"
        private const val incorrectAddress = "F0:AC:D7:C0:00:02"
        private const val someOtherAddress = "F0:AC:D7:C0:00:03"
        private const val notScannerAddress = "AA:AA:AA:AA:AA:AA"
        private const val correctSerial = "SP000001"
        private const val incorrectSerial = "SP000002"
        private const val someOtherSerial = "SP000003"
        private val correctGeneration = FingerprintConfiguration.VeroGeneration.VERO_2
        private val incorrectGeneration = FingerprintConfiguration.VeroGeneration.VERO_1
    }
}
