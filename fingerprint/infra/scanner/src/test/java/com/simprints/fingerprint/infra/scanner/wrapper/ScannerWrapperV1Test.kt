package com.simprints.fingerprint.infra.scanner.wrapper

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.domain.BatteryInfo.Companion.UNKNOWN
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerLowBatteryException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR
import com.simprints.fingerprint.infra.scanner.v1.Scanner
import com.simprints.fingerprint.infra.scanner.v1.ScannerCallback
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ScannerWrapperV1Test {
    private lateinit var scannerWrapper: ScannerWrapperV1

    @MockK
    lateinit var scanner: Scanner
    private var captureCallback = slot<ScannerCallback>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapper = ScannerWrapperV1(scanner, UnconfinedTestDispatcher())
    }

    @Test(expected = Test.None::class)
    fun `should complete successfully when the sensor shuts down without errors`() = runTest {
        // Given
        every { scanner.un20Shutdown(capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess()
        }
        // When
        scannerWrapper.sensorShutDown()
    }

    @Test
    fun shouldRead_scannerVersion_correctlyFormatted_withNewApiFormat() {
        // Given
        every { scanner.ucVersion } returns 1
        every { scanner.unVersion } returns 2
        val expectedVersionInfo = ScannerVersion(
            hardwareVersion = "",
            generation = ScannerGeneration.VERO_1,
            firmware = ScannerFirmwareVersions(
                cypress = "",
                stm = "1",
                un20 = "2",
            ),
        )

        // When
        val actualVersionInfo = scannerWrapper.versionInformation()

        // Then
        assertEquals(expectedVersionInfo, actualVersionInfo)
    }

    @Test
    fun `test imageTransfer shouldn't  be supported in v1 scanners`() {
        assertThat(scannerWrapper.isImageTransferSupported()).isFalse()
    }

    @Test
    fun `should throw ScannerLowBatteryException when UN20_LOW_VOLTAGE scanner error is returned during sensor wakeup`() = runTest {
        every { scanner.un20Wakeup(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.UN20_LOW_VOLTAGE)
        }

        assertThrows<ScannerLowBatteryException> { scannerWrapper.sensorWakeUp() }
    }

    @Test
    fun `should throw UnknownScannerIssueException when other scanner error occurs during sensor wakeup`() = runTest {
        every { scanner.un20Wakeup(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.IO_ERROR)
        }

        assertThrows<UnknownScannerIssueException> { scannerWrapper.sensorWakeUp() }
    }

    @Test(expected = UnknownScannerIssueException::class)
    fun `should throw unknown scanner exception when the sensor wakeUp completes with an error`() = runTest {
        // Given
        every { scanner.un20Wakeup(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.UN20_INVALID_STATE)
        }
        // When
        scannerWrapper.sensorWakeUp()
    }

    @Test(expected = ScannerLowBatteryException::class)
    fun `should throw low-battery exception when the sensor wakeUp completes with low-voltage error`() = runTest {
        // Given
        every { scanner.un20Wakeup(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.UN20_LOW_VOLTAGE)
        }
        // When
        scannerWrapper.sensorWakeUp()
    }

    @Test
    fun `should throw BluetoothNotEnabledException when BLUETOOTH_DISABLED scanner error is returned during connection`() = runTest {
        every { scanner.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.BLUETOOTH_DISABLED)
        }

        assertThrows<BluetoothNotEnabledException> { scannerWrapper.connect() }
    }

    @Test
    fun `should throw BluetoothNotSupportedException when BLUETOOTH_NOT_SUPPORTED scanner error is returned during connection`() = runTest {
        every { scanner.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED)
        }

        assertThrows<BluetoothNotSupportedException> { scannerWrapper.connect() }
    }

    @Test
    fun `should throw ScannerNotPairedException when SCANNER_UNBONDED scanner error is returned during connection`() = runTest {
        every { scanner.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.SCANNER_UNBONDED)
        }

        assertThrows<ScannerNotPairedException> { scannerWrapper.connect() }
    }

    @Test
    fun `should throw ScannerDisconnectedException when BUSY or IO_ERROR scanner error is returned during connection`() = runTest {
        every { scanner.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.BUSY)
        } andThenAnswer {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.IO_ERROR)
        }

        // SCANNER_ERROR.BUSY error is returned on first call
        assertThrows<ScannerDisconnectedException> { scannerWrapper.connect() }
        // SCANNER_ERROR.IO_ERROR error is returned on second call
        assertThrows<ScannerDisconnectedException> { scannerWrapper.connect() }
    }

    @Test
    fun `should throw UnknownScannerIssueException when non-connection related scanner error is returned during connection`() = runTest {
        every { scanner.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.UN20_FAILURE)
        }

        assertThrows<UnknownScannerIssueException> { scannerWrapper.connect() }
    }

    @Test
    fun `test batteryInformation should return UNKNOWN`() {
        assertThat(scannerWrapper.batteryInformation()).isEqualTo(UNKNOWN)
    }

    @Test
    fun `should throw the correct corresponding errors when scanner disconnection fails`() = runTest {
        coEvery { scanner.disconnect(any()) } answers {
            throw ScannerDisconnectedException()
        } andThenThrows (UnexpectedScannerException("Scanner cannot disconnect"))

        assertThrows<ScannerDisconnectedException> { scannerWrapper.disconnect() }
        assertThrows<UnexpectedScannerException> { scannerWrapper.disconnect() }
    }

    @Test
    fun `should success scanner disconnection completes`() = runTest {
        coEvery { scanner.disconnect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onSuccess()
        }
        scannerWrapper.disconnect()
    }

    @Test
    fun `should throw the correct corresponding errors when scanner connection fails`() = runTest {
        coEvery { scanner.connect(any()) } answers {
            throw ScannerDisconnectedException()
        } andThenAnswer {
            throw ScannerNotPairedException()
        } andThenAnswer {
            throw BluetoothNotEnabledException()
        } andThenThrows (BluetoothNotSupportedException())

        assertThrows<ScannerDisconnectedException> { scannerWrapper.connect() }
        assertThrows<ScannerNotPairedException> { scannerWrapper.connect() }
        assertThrows<BluetoothNotEnabledException> { scannerWrapper.connect() }
        assertThrows<BluetoothNotSupportedException> { scannerWrapper.connect() }
    }

    @Test(expected = Test.None::class)
    fun `should complete if no connection errors `() = runTest {
        coEvery { scanner.connect(any()) } answers {
            val callback = args[0] as ScannerCallback
            callback.onSuccess()
        }
        scannerWrapper.connect()
    }

    @Test(expected = UnknownScannerIssueException::class)
    fun `should throw ScannerDisconnectedException if getUn20Status throws IO`() = runTest {
        every { scanner.un20Wakeup(any()) } answers {
            val callback = args[0] as ScannerCallback
            callback.onFailure(SCANNER_ERROR.IO_ERROR)
        }
        scannerWrapper.sensorWakeUp()
    }

    @Test
    fun `isConnected() correctly passes scanner connection status`() {
        every { scanner.isConnected } returns false
        assertThat(scannerWrapper.isConnected()).isFalse()

        every { scanner.isConnected } returns true
        assertThat(scannerWrapper.isConnected()).isTrue()
    }
}
