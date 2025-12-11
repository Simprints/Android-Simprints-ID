package com.simprints.fingerprint.capture.state

import com.simprints.core.domain.reference.TemplateIdentifier

internal data class FingerState(
    val id: TemplateIdentifier,
    val captures: List<CaptureState>,
    val currentCaptureIndex: Int = 0,
) {
    fun isMultiCapture(): Boolean = captures.size > 1

    fun currentCapture(): CaptureState = captures[currentCaptureIndex]
}
