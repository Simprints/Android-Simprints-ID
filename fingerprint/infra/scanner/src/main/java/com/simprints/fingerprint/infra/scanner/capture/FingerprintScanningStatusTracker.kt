package com.simprints.fingerprint.infra.scanner.capture

import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.Idle
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.ImageQualityChecking.Bad
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.ImageQualityChecking.Good
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.ScanCompleted
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.Scanning
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FingerprintScanningStatusTracker @Inject constructor() {
    private val _state =
        MutableSharedFlow<FingerprintScanState>(replay = 1, extraBufferCapacity = 1)
    val state: SharedFlow<FingerprintScanState> = _state

    fun startScanning() {
        _state.tryEmit(Scanning)
    }

    fun completeScan() {
        _state.tryEmit(ScanCompleted)
    }

    fun setImageQualityCheckingResult(isQualityOk: Boolean) {
        _state.tryEmit(if (isQualityOk) Good else Bad)
    }

    fun resetToIdle() {
    _state.tryEmit(Idle)
    }
}

