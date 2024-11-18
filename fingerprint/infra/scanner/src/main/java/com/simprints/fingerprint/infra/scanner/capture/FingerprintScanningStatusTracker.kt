package com.simprints.fingerprint.infra.scanner.capture

import com.simprints.core.ExternalScope
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.Idle
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.ImageQualityChecking.Bad
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.ImageQualityChecking.Good
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.ScanCompleted
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanState.Scanning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FingerprintScanningStatusTracker @Inject constructor(
    @ExternalScope private val externalScope: CoroutineScope,
) {

    private val _state =
        MutableSharedFlow<FingerprintScanState>(replay = 1, extraBufferCapacity = 1)
    val state: SharedFlow<FingerprintScanState> = _state

    fun startScanning() = emitState(Scanning)

    fun completeScan() = emitState(ScanCompleted)

    fun setImageQualityCheckingResult(isQualityOk: Boolean) =
        emitState(if (isQualityOk) Good else Bad)

    fun resetToIdle() = emitState(Idle)

    fun emitState(state: FingerprintScanState) {
        externalScope.launch {
            _state.emit(state)
        }
    }
}
