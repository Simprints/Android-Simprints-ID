package com.simprints.fingerprint.scanner.wrapper


import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.scanner.exceptions.safe.*
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR
import com.simprints.fingerprintscanner.v1.Scanner
import com.simprints.fingerprintscanner.v1.ScannerCallback
import io.mockk.*
import io.mockk.impl.annotations.MockK
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

    @Test
    fun `test captureFingerprint success`() {
        // Given
        every { scanner.startContinuousCapture(any(), any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess()
        }
        // When
        val testObserver = getTestObserver()
        // Then
        testObserver.assertNoErrors()
        testObserver.assertComplete()

    }
    @Test
    fun `test captureFingerprint ScannerOperationInterruptedException`() {
        // Given
        mockScannerError(SCANNER_ERROR.INTERRUPTED)
        // When
        val testObserver = getTestObserver()
        // Then
        testObserver.assertError(ScannerOperationInterruptedException::class.java)
    }
    @Test
    fun `test captureFingerprint NoFingerDetectedException`() {
        // Given
        mockScannerError(SCANNER_ERROR.UN20_SDK_ERROR)
        // When
        val testObserver = getTestObserver()
        // Then
        testObserver.assertError(NoFingerDetectedException::class.java)
    }
    @Test
    fun `test captureFingerprint ScannerDisconnectedException`() {
        // Given
        mockScannerError(SCANNER_ERROR.INVALID_STATE)
        // When
        val testObserver = getTestObserver()
        // Then
        testObserver.assertError(ScannerDisconnectedException::class.java)
    }
    @Test
    fun `test captureFingerprint UnexpectedScannerException`() {
        // Given
        mockScannerError(SCANNER_ERROR.BLUETOOTH_DISABLED)
        // When
        val testObserver = getTestObserver()
        // Then
        testObserver.assertError(UnexpectedScannerException::class.java)
    }

    @Test
    fun `should complete successfully when the sensor shuts down without errors`() {
        // Given
        every { scanner.un20Shutdown(capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess()
        }
        // When
        val testObserver = scannerWrapper.sensorShutDown().test()
        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    fun `should throw error when the sensor shutdown completes with an error`() {
        // Given
        every { scanner.un20Shutdown(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.UN20_INVALID_STATE)
        }
        // When
        val testObserver = scannerWrapper.sensorShutDown().test()
        // Then
        testObserver.assertError {
            it is UnknownScannerIssueException
        }
    }

    @Test
    fun `should complete successfully when the sensor wakeUp completes without errors`() {
        // Given
        every { scanner.un20Wakeup(capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess()
        }
        // When
        val testObserver = scannerWrapper.sensorWakeUp().test()
        // Then
        testObserver.assertComplete()
    }

    @Test
    fun `should throw unknown scanner exception when the sensor wakeUp completes with an error`() {
        // Given
        every { scanner.un20Wakeup(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.UN20_INVALID_STATE)
        }
        // When
        val testObserver = scannerWrapper.sensorWakeUp().test()
        // Then
        testObserver.assertError {
            it is UnknownScannerIssueException
        }
    }

    @Test
    fun `should throw low-battery exception when the sensor wakeUp completes with low-voltage error`() {
        // Given
        every { scanner.un20Wakeup(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.UN20_LOW_VOLTAGE)
        }
        // When
        val testObserver = scannerWrapper.sensorWakeUp().test()
        // Then
        testObserver.assertError {
            it is ScannerLowBatteryException
        }
    }

    @Test
    fun `should complete successfully when connecting to the scanner completes without errors`() {
        // Given
        every { scanner.connect(capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess()
        }
        // When
        val testObserver = scannerWrapper.connect().test()
        // Then
        testObserver.assertComplete()
    }


    @Test
    fun `should throw BluetoothNotEnabledException exception when connecting to the scanner returns BLUETOOTH_DISABLED error`() {
        // Given
        every { scanner.connect(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.BLUETOOTH_DISABLED)
        }
        // When
        val testObserver = scannerWrapper.connect().test()
        // Then
        testObserver.assertError {
            it is BluetoothNotEnabledException
        }
    }


    @Test
    fun `should throw BluetoothNotSupportedException exception when connecting to the scanner returns BLUETOOTH_NOT_SUPPORTED error`() {
        // Given
        every { scanner.connect(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED)
        }
        // When
        val testObserver = scannerWrapper.connect().test()
        // Then
        testObserver.assertError {
            it is BluetoothNotSupportedException
        }
    }


    @Test
    fun `should throw ScannerNotPairedException exception when connecting to the scanner returns SCANNER_UNBONDED error`() {
        // Given
        every { scanner.connect(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.SCANNER_UNBONDED)
        }
        // When
        val testObserver = scannerWrapper.connect().test()
        // Then
        testObserver.assertError {
            it is ScannerNotPairedException
        }
    }

    @Test
    fun `should throw ScannerDisconnectedException exception when connecting to the scanner returns BUSY or IO_ERROR error`() {
        // Given
        every { scanner.connect(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.IO_ERROR)
        } andThenAnswer {
            captureCallback.captured.onFailure(SCANNER_ERROR.BUSY)
        }
        // When
        val testObserver = scannerWrapper.connect().test()
        val testObserver2 = scannerWrapper.connect().test()
        // Then
        testObserver.assertError {
            it is ScannerDisconnectedException
        }
        testObserver2.assertError {
            it is ScannerDisconnectedException
        }
    }

    @Test
    fun `should complete successfully regardless of response from setting UI as idle on scanner`() {
        // Given
        every { scanner.resetUI(capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(SCANNER_ERROR.UN20_INVALID_STATE)
        } andThenAnswer {
            captureCallback.captured.onSuccess()
        }
        // When
        val testObserver = scannerWrapper.setUiIdle().test()
        val testObserver2 = scannerWrapper.setUiIdle().test()
        // Then
        testObserver.assertComplete()
        testObserver2.assertComplete()
    }

    private fun mockScannerError(scannerError: SCANNER_ERROR) {
        every { scanner.startContinuousCapture(any(), any(), capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(scannerError)
        }
    }

    private fun getTestObserver() = scannerWrapper.captureFingerprint(
        CaptureFingerprintStrategy.SECUGEN_ISO_1000_DPI,
        CollectFingerprintsViewModel.scanningTimeoutMs.toInt(),
        60
    ).test()
}
