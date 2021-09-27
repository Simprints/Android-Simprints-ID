package com.simprints.fingerprint.scanner.wrapper

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerApiVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.*
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR
import com.simprints.fingerprintscanner.v1.ScannerCallback
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import com.simprints.fingerprintscanner.v1.ButtonListener as ScannerTriggerListenerV1
import com.simprints.fingerprintscanner.v1.Scanner as ScannerV1

class ScannerWrapperV1Test {
    @MockK lateinit var scannerV1: ScannerV1
    private lateinit var scannerWrapperV1: ScannerWrapperV1

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapperV1 = ScannerWrapperV1(scannerV1)
    }


    @Test
    @ExperimentalCoroutinesApi
    fun `should return the correct vero 1 scanner info`() = runBlockingTest {
        val ucVersion = 3
        val unVersion = 4
        val expectedVersion = ScannerVersion(
            ScannerGeneration.VERO_1,
            ScannerFirmwareVersions(
                cypress = ChipFirmwareVersion.UNKNOWN,
                stm = ChipFirmwareVersion(ucVersion, 0),
                un20 = ChipFirmwareVersion(unVersion, 0)
            ),
            ScannerApiVersions.UNKNOWN
        )
        every { scannerV1.ucVersion } returns ucVersion.toShort()
        every { scannerV1.unVersion } returns unVersion.toShort()


        val actualVersion = scannerWrapperV1.versionInformation()

        assertThat(actualVersion).isEqualTo(expectedVersion)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should continue successfully when setting scanner LED to idle completes successfully or fails`() = runBlockingTest {
        // trigger the button click listener callback when it is registered
        every { scannerV1.disconnect(any()) } answers {
            val scannerCallback = args.last() as ScannerCallback
            scannerCallback.onSuccess()
        } andThen {
            val scannerCallback = args.last() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.IO_ERROR)
        }

        // disconnecting successfully, should continue without issues
        scannerWrapperV1.disconnect()
        // disconnecting when error occurs, should continue without issues
        scannerWrapperV1.disconnect()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should continue successfully if scanner disconnection fails or succeeds`() = runBlockingTest {
        // trigger the button click listener callback when it is registered
        every { scannerV1.disconnect(any()) } answers {
            val scannerCallback = args.last() as ScannerCallback
            scannerCallback.onSuccess()
        } andThen {
            val scannerCallback = args.last() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.IO_ERROR)
        }

        // disconnecting successfully, should continue without issues
        scannerWrapperV1.disconnect()
        // disconnecting when error occurs, should continue without issues
        scannerWrapperV1.disconnect()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should unregister specified callback for scan-trigger`() = runBlockingTest {
        // trigger the button click listener callback when it is registered
        every { scannerV1.unregisterButtonListener(any()) } returns true
        val scanListener = mockk<ScannerTriggerListener>(relaxed = true)

        // first register callback and then unregister callback
        scannerWrapperV1.registerTriggerListener(scanListener)
        scannerWrapperV1.unregisterTriggerListener(scanListener)

        // verify that the actual listener was invoked
        verify(exactly = 1) { scannerV1.unregisterButtonListener(any()) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should register appropriate callback for scan-trigger`() = runBlockingTest {
        // trigger the button click listener callback when it is registered
        every { scannerV1.registerButtonListener(any()) } answers {
            val listener = args.first() as ScannerTriggerListenerV1
            listener.onClick()
            true
        }

        val scanListener = mockk<ScannerTriggerListener>(relaxed = true)
        scannerWrapperV1.registerTriggerListener(scanListener)

        // verify that the actual listener was invoked
        verify(exactly = 1) { scanListener.onTrigger() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should force a fingerprint capture when timeout occurs during continuous capture`() = runBlockingTest {
        every { scannerV1.template } returns byteArrayOf()
        every { scannerV1.imageQuality } returns 64
        every { scannerV1.startContinuousCapture(any(), any(), any()) } answers {
            val scannerCallback = args.last() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.TIMEOUT)
        }
        every { scannerV1.forceCapture(any(), any()) } answers {
            val scannerCallback = args.last() as ScannerCallback
            scannerCallback.onSuccess()
        }

        scannerWrapperV1.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
            1000,
            50
        )

        verify(exactly = 1) { scannerV1.forceCapture(any(), any()) }
    }



    @Test
    @ExperimentalCoroutinesApi
    fun `should return appropriate CaptureFingerprintResponse when captureFingerprint returns successfully`() = runBlockingTest {
        val expectedResponse = CaptureFingerprintResponse(byteArrayOf(), 64)
        every { scannerV1.template } returns expectedResponse.template
        every { scannerV1.imageQuality } returns expectedResponse.imageQualityScore

        every { scannerV1.startContinuousCapture(any(), any(), any()) } answers {
            val scannerCallback = args.last() as ScannerCallback
            scannerCallback.onSuccess()
        }

        val actualResponse = scannerWrapperV1.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
            1000,
            50
        )

        assertThat(actualResponse.imageQualityScore).isEqualTo(expectedResponse.imageQualityScore)
        assertThat(actualResponse.template).isEqualTo(expectedResponse.template)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should trigger scanner's stopContinuousCapture when captureFingerprint coroutine is cancelled`() = runBlockingTest {
        val expectedResponse = CaptureFingerprintResponse(byteArrayOf(), 64)
        every { scannerV1.template } returns expectedResponse.template
        every { scannerV1.imageQuality } returns expectedResponse.imageQualityScore

        every { scannerV1.stopContinuousCapture() } returns true

        val captureJob = launch {
            scannerWrapperV1.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                50
            )
        }

        // cancel capture
        captureJob.cancel()

        // ensure that scanner's continuous capture is stopped
        verify(exactly = 1) { scannerV1.stopContinuousCapture() }
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
    @ExperimentalCoroutinesApi
    fun `should throw UnknownScannerIssueException when any scanner error is returned during sensor shutdown`() = runBlockingTest {
        every { scannerV1.un20Shutdown(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.UN20_INVALID_STATE)
        }
        assertThrows<UnknownScannerIssueException> { scannerWrapperV1.sensorShutDown() }

    }

    @Test
    fun `test imageTransfer shouldn't  be supported in v1  scanners`() {
        Truth.assertThat(scannerWrapper.isImageTransferSupported()).isFalse()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw ScannerLowBatteryException when UN20_LOW_VOLTAGE scanner error is returned during sensor wakeup`() = runBlockingTest {
        every { scannerV1.un20Wakeup(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.UN20_LOW_VOLTAGE)
        }

        assertThrows<ScannerLowBatteryException> { scannerWrapperV1.sensorWakeUp() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw UnknownScannerIssueException when other scanner error occurs during sensor wakeup`() = runBlockingTest {
        every { scannerV1.un20Wakeup(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.IO_ERROR)
        }

        assertThrows<UnknownScannerIssueException> { scannerWrapperV1.sensorWakeUp() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw BluetoothNotEnabledException when BLUETOOTH_DISABLED scanner error is returned during connection`() = runBlockingTest {
        every { scannerV1.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.BLUETOOTH_DISABLED)
        }

        assertThrows<BluetoothNotEnabledException> { scannerWrapperV1.connect() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw BluetoothNotSupportedException when BLUETOOTH_NOT_SUPPORTED scanner error is returned during connection`() = runBlockingTest {
        every { scannerV1.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED)
        }

        assertThrows<BluetoothNotSupportedException> { scannerWrapperV1.connect() }
    }


    @Test
    @ExperimentalCoroutinesApi
    fun `should throw ScannerNotPairedException when SCANNER_UNBONDED scanner error is returned during connection`() = runBlockingTest {
        every { scannerV1.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.SCANNER_UNBONDED)
        }

        assertThrows<ScannerNotPairedException> { scannerWrapperV1.connect() }
    }


    @Test
    @ExperimentalCoroutinesApi
    fun `should throw ScannerDisconnectedException when BUSY or IO_ERROR scanner error is returned during connection`() = runBlockingTest {
        every { scannerV1.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.BUSY)
        } andThen {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.IO_ERROR)
        }

        // SCANNER_ERROR.BUSY error is returned on first call
        assertThrows<ScannerDisconnectedException> { scannerWrapperV1.connect() }
        // SCANNER_ERROR.IO_ERROR error is returned on second call
        assertThrows<ScannerDisconnectedException> { scannerWrapperV1.connect() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw UnknownScannerIssueException when non-connection related scanner error is returned during connection`() = runBlockingTest {
        every { scannerV1.connect(any()) } answers {
            val scannerCallback = args.first() as ScannerCallback
            scannerCallback.onFailure(SCANNER_ERROR.UN20_FAILURE)
        }

        assertThrows<UnknownScannerIssueException> { scannerWrapperV1.connect() }
    }


//    private var captureCallback = slot<ScannerCallback>()
//
//
//    @Test
//    fun `test captureFingerprint success`() {
//        // Given
//        every { scanner.startContinuousCapture(any(), any(), capture(captureCallback)) } answers {
//            captureCallback.captured.onSuccess()
//        }
//        // When
//        val testObserver = getTestObserver()
//        // Then
//        testObserver.assertNoErrors()
//        testObserver.assertComplete()
//
//    }
//    @Test
//    fun `test captureFingerprint ScannerOperationInterruptedException`() {
//        // Given
//        mockScannerError(SCANNER_ERROR.INTERRUPTED)
//        // When
//        val testObserver = getTestObserver()
//        // Then
//        testObserver.assertError(ScannerOperationInterruptedException::class.java)
//    }
//    @Test
//    fun `test captureFingerprint NoFingerDetectedException`() {
//        // Given
//        mockScannerError(SCANNER_ERROR.UN20_SDK_ERROR)
//        // When
//        val testObserver = getTestObserver()
//        // Then
//        testObserver.assertError(NoFingerDetectedException::class.java)
//    }
//    @Test
//    fun `test captureFingerprint ScannerDisconnectedException`() {
//        // Given
//        mockScannerError(SCANNER_ERROR.INVALID_STATE)
//        // When
//        val testObserver = getTestObserver()
//        // Then
//        testObserver.assertError(ScannerDisconnectedException::class.java)
//    }
//    @Test
//    fun `test captureFingerprint UnexpectedScannerException`() {
//        // Given
//        mockScannerError(SCANNER_ERROR.BLUETOOTH_DISABLED)
//        // When
//        val testObserver = getTestObserver()
//        // Then
//        testObserver.assertError(UnexpectedScannerException::class.java)
//    }
//
//    private fun mockScannerError(scannerError: SCANNER_ERROR) {
//        every { scanner.startContinuousCapture(any(), any(), capture(captureCallback)) } answers {
//            captureCallback.captured.onFailure(scannerError)
//        }
//    }
//
//    private fun getTestObserver() = scannerWrapper.captureFingerprint(
//        CaptureFingerprintStrategy.SECUGEN_ISO_1000_DPI,
//        CollectFingerprintsViewModel.scanningTimeoutMs.toInt(),
//        60
//    ).test()
}
