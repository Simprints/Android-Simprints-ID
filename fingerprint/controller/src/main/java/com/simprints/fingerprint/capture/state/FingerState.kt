package com.simprints.fingerprint.capture.state

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

internal data class FingerState(
    val id: FingerIdentifier,
    val captures: List<CaptureState>,
    val currentCaptureIndex: Int = 0
) {

    fun isMultiCapture(): Boolean = captures.size > 1
    fun currentCapture(): CaptureState = captures[currentCaptureIndex]
}
