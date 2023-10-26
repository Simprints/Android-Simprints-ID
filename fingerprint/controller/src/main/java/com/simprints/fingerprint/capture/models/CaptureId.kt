package com.simprints.fingerprint.capture.models

import com.simprints.moduleapi.fingerprint.IFingerIdentifier

internal data class CaptureId(
    val finger: IFingerIdentifier,
    val captureIndex: Int,
)
