package com.simprints.fingerprint.infra.scanner.capture

import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR
import com.simprints.fingerprint.infra.scanner.v1.Scanner
import com.simprints.fingerprint.infra.scanner.v1.ScannerCallback
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FingerprintCaptureWrapperV1Test {

    private lateinit var scannerWrapper: FingerprintCaptureWrapperV1

    @MockK
    lateinit var scanner: Scanner

    private var captureCallback = slot<ScannerCallback>()


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapper =
            FingerprintCaptureWrapperV1(scanner, UnconfinedTestDispatcher())
    }

    @Test(expected = ScannerOperationInterruptedException::class)
    fun `test captureFingerprint ScannerOperationInterruptedException`() = runTest {
        mockScannerError(SCANNER_ERROR.INTERRUPTED)
        startCapturing()
    }

    @Test(expected = NoFingerDetectedException::class)
    fun `test captureFingerprint NoFingerDetectedException`() = runTest {
        mockScannerError(SCANNER_ERROR.UN20_SDK_ERROR)
        startCapturing()
    }

    @Test(expected = Test.None::class)
    fun `test captureFingerprint should complete successfully after a TIMEOUT`() = runTest {
        // Given
        mockScannerError(SCANNER_ERROR.TIMEOUT)
        every { scanner.forceCapture(any(), any()) } answers {
            val scannerCallback = args[1] as ScannerCallback
            scannerCallback.onSuccess()
        }
        // When
        startCapturing()
        // Then
        verify { scanner.forceCapture(any(), any()) }
    }

    @Test(expected = ScannerDisconnectedException::class)
    fun `test captureFingerprint ScannerDisconnectedException`() = runTest {
        mockScannerError(SCANNER_ERROR.INVALID_STATE)
        startCapturing()
    }

    @Test(expected = UnexpectedScannerException::class)
    fun `test captureFingerprint UnexpectedScannerException`() = runTest {
        mockScannerError(SCANNER_ERROR.BLUETOOTH_DISABLED)
        startCapturing()
    }

    @Test
    fun `test captureFingerprint success`() = runTest {
        // Given
        every { scanner.startContinuousCapture(any(), any(), capture(captureCallback)) } answers {
            captureCallback.captured.onSuccess()
        }
        startCapturing()
    }

    private fun mockScannerError(scannerError: SCANNER_ERROR) {
        every { scanner.startContinuousCapture(any(), any(), capture(captureCallback)) } answers {
            captureCallback.captured.onFailure(scannerError)

        }
    }

    private suspend fun startCapturing() = scannerWrapper.acquireFingerprintTemplate(
        Dpi(1000),
        3000,
        60,
        false
    )

}
