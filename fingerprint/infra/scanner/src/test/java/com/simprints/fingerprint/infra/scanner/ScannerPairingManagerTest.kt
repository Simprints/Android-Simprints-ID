package com.simprints.fingerprint.infra.scanner

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.infra.scannermock.dummy.DummyBluetoothDevice
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RunWith(AndroidJUnit4::class)
class ScannerPairingManagerTest {
    private val bluetoothAdapterMock = mockk<ComponentBluetoothAdapter>()
    private val scannerGenerationDeterminerMock = mockk<ScannerGenerationDeterminer> {
        every { determineScannerGenerationFromSerialNumber(eq(CORRECT_SERIAL)) } returns correctGeneration
        every { determineScannerGenerationFromSerialNumber(eq(INCORRECT_SERIAL)) } returns incorrectGeneration
    }
    private val serialNumberConverterMock = mockk<SerialNumberConverter> {
        every { convertMacAddressToSerialNumber(eq(CORRECT_ADDRESS)) } returns CORRECT_SERIAL
        every { convertMacAddressToSerialNumber(eq(INCORRECT_ADDRESS)) } returns INCORRECT_SERIAL
        every { convertMacAddressToSerialNumber(eq(SOME_OTHER_ADDRESS)) } returns SOME_OTHER_SERIAL
        every { convertSerialNumberToMacAddress(eq(CORRECT_SERIAL)) } returns CORRECT_ADDRESS
        every { convertSerialNumberToMacAddress(eq(INCORRECT_SERIAL)) } returns INCORRECT_ADDRESS
        every { convertSerialNumberToMacAddress(eq(SOME_OTHER_SERIAL)) } returns SOME_OTHER_ADDRESS
    }
    private val recentUserActivity = mockk<RecentUserActivity>()
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns recentUserActivity
    }
    private val fingerprintConfiguration = mockk<FingerprintConfiguration>()
    private val configRepository = mockk<ConfigRepository> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
    }
    private val scannerPairingManager = ScannerPairingManager(
        bluetoothAdapterMock,
        recentUserActivityManager,
        scannerGenerationDeterminerMock,
        serialNumberConverterMock,
        configRepository,
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
                "12345",
            )
        }
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "9876543",
            )
        }
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "-12345",
            )
        }
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "1234o2",
            )
        }
        assertThrows<NumberFormatException> {
            scannerPairingManager.interpretEnteredTextAsSerialNumber(
                "1874.5",
            )
        }
    }

    @Test
    fun getPairedScannerAddressToUse_oneValidPairedDevice_returnsCorrectly() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = CORRECT_ADDRESS))
        every { fingerprintConfiguration.allowedScanners } returns listOf(correctGeneration)

        assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(CORRECT_ADDRESS)
    }

    @Test
    fun getPairedScannerAddressToUse_oneInvalidPairedDevice_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = INCORRECT_ADDRESS))
        every { fingerprintConfiguration.allowedScanners } returns listOf(correctGeneration)

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_noScannersPaired_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(DummyBluetoothDevice(address = NOT_SCANNER_ADDRESS))

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_noDevicesPaired_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf()

        assertThrows<ScannerNotPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_multipleDevicesWithOneValidScanner_returnsCorrectly() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = CORRECT_ADDRESS),
            DummyBluetoothDevice(address = INCORRECT_ADDRESS),
            DummyBluetoothDevice(address = NOT_SCANNER_ADDRESS),
        )
        every { fingerprintConfiguration.allowedScanners } returns listOf(
            correctGeneration,
        )

        assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(
            CORRECT_ADDRESS,
        )
    }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_lastScannerUsedExistsAndIsPaired() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = CORRECT_ADDRESS),
            DummyBluetoothDevice(address = INCORRECT_ADDRESS),
            DummyBluetoothDevice(address = NOT_SCANNER_ADDRESS),
        )
        every { fingerprintConfiguration.allowedScanners } returns listOf(
            correctGeneration,
            incorrectGeneration,
        )
        every { recentUserActivity.lastScannerUsed } returns CORRECT_SERIAL

        assertThat(scannerPairingManager.getPairedScannerAddressToUse()).isEqualTo(
            CORRECT_ADDRESS,
        )
    }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_noLastScannerUsed_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = CORRECT_ADDRESS),
            DummyBluetoothDevice(address = INCORRECT_ADDRESS),
            DummyBluetoothDevice(address = NOT_SCANNER_ADDRESS),
        )
        every { fingerprintConfiguration.allowedScanners } returns listOf(
            correctGeneration,
            incorrectGeneration,
        )
        every { recentUserActivity.lastScannerUsed } returns ""

        assertThrows<MultiplePossibleScannersPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun getPairedScannerAddressToUse_multipleValidDevices_lastScannerUsedNotPaired_throws() = runTest {
        every { bluetoothAdapterMock.getBondedDevices() } returns setOf(
            DummyBluetoothDevice(address = CORRECT_ADDRESS),
            DummyBluetoothDevice(address = INCORRECT_ADDRESS),
            DummyBluetoothDevice(address = NOT_SCANNER_ADDRESS),
        )
        every { fingerprintConfiguration.allowedScanners } returns listOf(
            correctGeneration,
            incorrectGeneration,
        )
        every { recentUserActivity.lastScannerUsed } returns SOME_OTHER_SERIAL

        assertThrows<MultiplePossibleScannersPairedException> { scannerPairingManager.getPairedScannerAddressToUse() }
    }

    @Test
    fun receivedNotBondedIntent_pairStateReceiver_doesNotReact() = runTest {
        suspendCoroutine { continuation ->
            val receiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
                onPairSuccess = { fail() },
                onPairFailed = { fail() },
            )
            receiver.onReceive(mockk(), createReceivedIntent())
            continuation.resume(Unit)
        }
    }

    @Test
    fun receivedBondRemovedIntent_pairStateReceiver_doesNotReact() = runTest {
        suspendCoroutine { continuation ->
            val receiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
                onPairSuccess = { fail() },
                onPairFailed = { fail() },
            )
            receiver.onReceive(
                mockk(),
                createReceivedIntent(
                    failReason = ComponentBluetoothDevice.UNBOND_REASON_REMOVED,
                ),
            )
            continuation.resume(Unit)
        }
    }

    @Test
    fun receivedBondedIntent_pairStateReceiver_triggersSuccess() = runTest {
        suspendCoroutine { continuation ->
            val receiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
                onPairSuccess = { continuation.resume(Unit) },
                onPairFailed = { fail() },
            )
            receiver.onReceive(mockk(), createReceivedIntent(state = ComponentBluetoothDevice.BOND_BONDED))
        }
    }

    @Test
    fun receivedPairingFailedIntent_pairStateReceiver_triggersFailWithFalse() = runTest {
        suspendCoroutine { continuation ->
            val receiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
                onPairSuccess = { fail() },
                onPairFailed = {
                    assertThat(it).isFalse()
                    continuation.resume(Unit)
                },
            )
            receiver.onReceive(
                mockk(),
                createReceivedIntent(
                    failReason = 5, // Not one of the "rejected" reason codes
                ),
            )
        }
    }

    @Test
    fun receivedPairingRejectedIntent_pairStateReceiver_triggersFailWithTrue() = runTest {
        listOf(
            ComponentBluetoothDevice.REASON_AUTH_FAILED,
            ComponentBluetoothDevice.REASON_AUTH_REJECTED,
            ComponentBluetoothDevice.REASON_AUTH_CANCELED,
            ComponentBluetoothDevice.REASON_REMOTE_AUTH_CANCELED,
        ).forEach { failReason ->
            suspendCoroutine { continuation ->
                val receiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
                    onPairSuccess = { fail() },
                    onPairFailed = {
                        assertThat(it).isTrue()
                        continuation.resume(Unit)
                    },
                )
                receiver.onReceive(mockk(), createReceivedIntent(failReason = failReason))
            }
        }
    }

    private fun createReceivedIntent(
        state: Int = ComponentBluetoothDevice.BOND_NONE,
        failReason: Int = ComponentBluetoothDevice.BOND_SUCCESS,
    ) = Intent(ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED).apply {
        putExtra(ComponentBluetoothDevice.EXTRA_BOND_STATE, state)
        putExtra(ComponentBluetoothDevice.EXTRA_REASON, failReason)
    }

    companion object {
        private const val CORRECT_ADDRESS = "F0:AC:D7:C0:00:01"
        private const val INCORRECT_ADDRESS = "F0:AC:D7:C0:00:02"
        private const val SOME_OTHER_ADDRESS = "F0:AC:D7:C0:00:03"
        private const val NOT_SCANNER_ADDRESS = "AA:AA:AA:AA:AA:AA"
        private const val CORRECT_SERIAL = "SP000001"
        private const val INCORRECT_SERIAL = "SP000002"
        private const val SOME_OTHER_SERIAL = "SP000003"
        private val correctGeneration = FingerprintConfiguration.VeroGeneration.VERO_2
        private val incorrectGeneration = FingerprintConfiguration.VeroGeneration.VERO_1
    }
}
