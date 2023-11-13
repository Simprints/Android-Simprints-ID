package com.simprints.fingerprint.capture.models

import com.simprints.core.domain.fingerprint.IFingerIdentifier

internal data class CaptureId(
    val finger: IFingerIdentifier,
    val captureIndex: Int,
)
