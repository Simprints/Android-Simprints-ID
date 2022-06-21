package com.simprints.fingerprint.scanner.wrapper


import com.google.common.truth.Truth
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
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
    fun `test imageTransfer shouldn't  be supported in v1  scanners`() {
        Truth.assertThat(scannerWrapper.isImageTransferSupported()).isFalse()
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
