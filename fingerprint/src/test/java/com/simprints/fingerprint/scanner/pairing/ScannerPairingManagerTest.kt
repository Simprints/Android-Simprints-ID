package com.simprints.fingerprint.scanner.pairing

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.exceptions.safe.MultipleScannersPairedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscannermock.dummy.DummyBluetoothDevice
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
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
    private val prefsMock = mockk<FingerprintPreferencesManager>()
    private val scannerPairingManager = ScannerPairingManager(bluetoothAdapterMock, prefsMock, scannerGenerationDeterminerMock, serialNumberConverterMock)

    @Test
    fun interpretEnteredTextAsSerialNumber_worksCorrectlyForValidStrings() {
        assertThat(scannerPairingManager.interpretEnteredTextAsSerialNumber("987654")).isEqualTo("SP987654")
        assertThat(scannerPairingManager.interpretEnteredTextAsSerialNumber("012345")).isEqualTo("SP012345")
    }

    @Test
    fun interpretEnteredTextAsSerialNumber_throwsForInvalidStrings() {
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("12345") }
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("9876543") }
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("-12345") }
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("1234o2") }
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("1874.5") }
    }

    @Test
    fun getPairedScannerAddressToUse_oneValidPairedDevice_returnsCorrectly() {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = correctAddress))
        every { prefsMock.scannerGenerations } returns listOf(correctGeneration)

        assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(correctAddress)
    }

    @Test
    fun getPairedScannerAddressToUse_oneInvalidPairedDevice_throws() {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = incorrectAddress))
        every { prefsMock.scannerGenerations } returns listOf(correctGeneration)

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_noScannersPaired_throws() {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = notScannerAddress))

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_noDevicesPaired_throws() {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf()

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_multipleDevicesWithOneValidScanner_returnsCorrectly() {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = correctAddress),
            DummyBluetoothDevice(address = incorrectAddress),
            DummyBluetoothDevice(address = notScannerAddress)
        )
        every { prefsMock.scannerGenerations } returns listOf(correctGeneration)

        assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(correctAddress)
    }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_lastScannerUsedExistsAndIsPaired() {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = correctAddress),
            DummyBluetoothDevice(address = incorrectAddress),
            DummyBluetoothDevice(address = notScannerAddress)
        )
        every { prefsMock.scannerGenerations } returns listOf(correctGeneration, incorrectGeneration)
        every { prefsMock.lastScannerUsed } returns correctSerial

        assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(correctAddress)
    }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_noLastScannerUsed_throws() {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = correctAddress),
            DummyBluetoothDevice(address = incorrectAddress),
            DummyBluetoothDevice(address = notScannerAddress)
        )
        every { prefsMock.scannerGenerations } returns listOf(correctGeneration, incorrectGeneration)
        every { prefsMock.lastScannerUsed } returns ""

        assertThrows<MultipleScannersPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_lastScannerUsedNotPaired_throws() {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = correctAddress),
            DummyBluetoothDevice(address = incorrectAddress),
            DummyBluetoothDevice(address = notScannerAddress)
        )
        every { prefsMock.scannerGenerations } returns listOf(correctGeneration, incorrectGeneration)
        every { prefsMock.lastScannerUsed } returns someOtherSerial

        assertThrows<MultipleScannersPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    companion object {
        private const val correctAddress = "F0:AC:D7:C0:00:01"
        private const val incorrectAddress = "F0:AC:D7:C0:00:02"
        private const val someOtherAddress = "F0:AC:D7:C0:00:03"
        private const val notScannerAddress = "AA:AA:AA:AA:AA:AA"
        private const val correctSerial = "SP000001"
        private const val incorrectSerial = "SP000002"
        private const val someOtherSerial = "SP000003"
        private val correctGeneration = ScannerGeneration.VERO_2
        private val incorrectGeneration = ScannerGeneration.VERO_1
    }
}
