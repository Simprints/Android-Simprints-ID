package com.simprints.fingerprint.infra.scanner.capture

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.NullScannerException
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import com.simprints.fingerprint.infra.scanner.v1.Scanner as ScannerV1
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner as ScannerV2

@Singleton
class FingerprintCaptureWrapperFactory @Inject constructor(
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
    private val scannerUiHelper: ScannerUiHelper,
    private val scanningStatusTracker: FingerprintScanningStatusTracker
) {
    private var _captureWrapper: FingerprintCaptureWrapper? = null

    val captureWrapper: FingerprintCaptureWrapper
        get() = _captureWrapper ?: throw NullScannerException()

    fun createV1(scannerV1: ScannerV1) {
        _captureWrapper =
            FingerprintCaptureWrapperV1(scannerV1, ioDispatcher, scanningStatusTracker)
    }

    fun createV2(scannerV2: ScannerV2) {
        _captureWrapper = FingerprintCaptureWrapperV2(
            scannerV2, scannerUiHelper, ioDispatcher, scanningStatusTracker
        )
    }
}
