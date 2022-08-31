package com.simprints.fingerprint.scanner.wrapper


import com.google.common.truth.Truth
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.*
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR
import com.simprints.fingerprintscanner.v1.Scanner
import com.simprints.fingerprintscanner.v1.ScannerCallback
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
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
        scannerWrapper = ScannerWrapperV1(scanner)
    }

    @Test(expected = ScannerOperationInterruptedException::class)
    fun `test captureFingerprint ScannerOperationInterruptedException`()= runTest {
        mockScannerError(SCANNER_ERROR.INTERRUPTED)
        startCapturing()
   }

    @Test(expected = NoFingerDetectedException::class)
    fun `test captureFingerprint NoFingerDetectedException`() = runTest{
        mockScannerError(SCANNER_ERROR.UN20_SDK_ERROR)
        startCapturing()
    }

    @Test(expected =ScannerDisconnectedException::class)
    fun `test captureFingerprint ScannerDisconnectedException`()= runTest{
        mockScannerError(SCANNER_ERROR.INVALID_STATE)
        startCapturing()
    }

    @Test(expected = UnexpectedScannerException::class)
    fun `test captureFingerprint UnexpectedScannerException`() = runTest{
        mockScannerError(SCANNER_ERROR.BLUETOOTH_DISABLED)
        startCapturing()
    }


    @Test(expected = Test.None::class)
    fun `should complete successfully when the sensor shuts down without errors`()= runTest {
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
                un20 = "2"
            )
        )

        // When
        val actualVersionInfo = scannerWrapper.versionInformation()

        // Then
        assertEquals(expectedVersionInfo, actualVersionInfo)
    }

    @Test
    fun `test captureFingerprint success`() = runTest {
        // Given
        every { scanner.startContinuousCapture(any(), any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess()
        }
        scannerWrapper.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1000_DPI,
            CollectFingerprintsViewModel.scanningTimeoutMs.toInt(),
            60
        )
    }

    @Test
    fun `test imageTransfer shouldn't  be supported in v1  scanners`() {
        Truth.assertThat(scannerWrapper.isImageTransferSupported()).isFalse()
    }

    @Test
    fun `should throw ScannerLowBatteryException when UN20_LOW_VOLTAGE scanner error is returned during sensor wakeup`() =
        runTest {
            every { scanner.un20Wakeup(any()) } answers {
                val scannerCallback = args.first() as ScannerCallback
                scannerCallback.onFailure(SCANNER_ERROR.UN20_LOW_VOLTAGE)
            }

            assertThrows<ScannerLowBatteryException> { scannerWrapper.sensorWakeUp() }
        }

    @Test
    fun `should throw UnknownScannerIssueException when other scanner error occurs during sensor wakeup`() =
        runTest {
            every { scanner.un20Wakeup(any()) } answers {
                val scannerCallback = args.first() as ScannerCallback
                scannerCallback.onFailure(SCANNER_ERROR.IO_ERROR)
            }

            assertThrows<UnknownScannerIssueException> { scannerWrapper.sensorWakeUp() }
        }
    @Test(expected = UnknownScannerIssueException::class)
    fun `should throw unknown scanner exception when the sensor wakeUp completes with an error`() =
        runTest{
        // Given
        every { scanner.un20Wakeup(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.UN20_INVALID_STATE)
        }
        // When
         scannerWrapper.sensorWakeUp()
        }

   @Test(expected = ScannerLowBatteryException::class)
    fun `should throw low-battery exception when the sensor wakeUp completes with low-voltage error`() =
       runTest{
        // Given
        every { scanner.un20Wakeup(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.UN20_LOW_VOLTAGE)
        }
        // When
         scannerWrapper.sensorWakeUp()
      }

    @Test
    fun `should throw BluetoothNotEnabledException when BLUETOOTH_DISABLED scanner error is returned during connection`() =
        runTest {
            every { scanner.connect(any()) } answers {
                val scannerCallback = args.first() as ScannerCallback
                scannerCallback.onFailure(SCANNER_ERROR.BLUETOOTH_DISABLED)
            }

            assertThrows<BluetoothNotEnabledException> { scannerWrapper.connect() }
        }

    @Test
    fun `should throw BluetoothNotSupportedException when BLUETOOTH_NOT_SUPPORTED scanner error is returned during connection`() =
        runTest {
            every { scanner.connect(any()) } answers {
                val scannerCallback = args.first() as ScannerCallback
                scannerCallback.onFailure(SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED)
            }

            assertThrows<BluetoothNotSupportedException> { scannerWrapper.connect() }
        }


    @Test
    fun `should throw ScannerNotPairedException when SCANNER_UNBONDED scanner error is returned during connection`() =
        runTest {
            every { scanner.connect(any()) } answers {
                val scannerCallback = args.first() as ScannerCallback
                scannerCallback.onFailure(SCANNER_ERROR.SCANNER_UNBONDED)
            }

            assertThrows<ScannerNotPairedException> { scannerWrapper.connect() }
        }


    @Test
    fun `should throw ScannerDisconnectedException when BUSY or IO_ERROR scanner error is returned during connection`() =
        runTest {
            every { scanner.connect(any()) } answers {
                val scannerCallback = args.first() as ScannerCallback
                scannerCallback.onFailure(SCANNER_ERROR.BUSY)
            } andThenAnswer  {
                val scannerCallback = args.first() as ScannerCallback
                scannerCallback.onFailure(SCANNER_ERROR.IO_ERROR)
            }

            // SCANNER_ERROR.BUSY error is returned on first call
            assertThrows<ScannerDisconnectedException> { scannerWrapper.connect() }
            // SCANNER_ERROR.IO_ERROR error is returned on second call
            assertThrows<ScannerDisconnectedException> { scannerWrapper.connect() }
        }

    @Test
    fun `should throw UnknownScannerIssueException when non-connection related scanner error is returned during connection`() =
        runTest {
            every { scanner.connect(any()) } answers {
                val scannerCallback = args.first() as ScannerCallback
                scannerCallback.onFailure(SCANNER_ERROR.UN20_FAILURE)
            }

            assertThrows<UnknownScannerIssueException> { scannerWrapper.connect() }
        }

    private fun mockScannerError(scannerError: SCANNER_ERROR) {
        every { scanner.startContinuousCapture(any(), any(), capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(scannerError)

        }
    }
    private suspend fun startCapturing() = scannerWrapper.captureFingerprint(
        CaptureFingerprintStrategy.SECUGEN_ISO_1000_DPI,
        CollectFingerprintsViewModel.scanningTimeoutMs.toInt(),
        60
    )

}
